package harvestLog.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryAiService {
    private final ChatClient chatClient;

    public CategoryAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Batch suggestion: for each cropName in cropNames, suggest either:
     *  - matchedExisting: exact name of one of existingCategories (case preserved from existingCategories list),
     *  - or suggestion: a new suggested short category name
     *
     * Returned list has same order as cropNames (one SuggestionResult per crop).
     */
    public List<SuggestionResult> suggestForBatch(List<String> cropNames, List<String> existingCategories) {
        Objects.requireNonNull(cropNames);
        if (cropNames.isEmpty()) return List.of();

        String existingList = (existingCategories == null || existingCategories.isEmpty())
                ? "NONE"
                : String.join(", ", existingCategories);

        // Build prompt enumerating crops, and require a strict, line-by-line output so we can parse reliably.
        StringBuilder sb = new StringBuilder();
        sb.append("You are a farm management assistant. For each crop below, select the best-matching category\n")
                .append("from the provided existing categories. If none match, reply with NONE and provide a short suggested\n")
                .append("category name. Do NOT invent very long names; keep suggestions short (1-2 words, example: FRUIT, VEGETABLE, GRAIN).\n\n");

        sb.append("Existing categories: ").append(existingList).append("\n\n");
        sb.append("Crops:\n");
        for (int i = 0; i < cropNames.size(); i++) {
            sb.append(i).append(": ").append(cropNames.get(i)).append("\n");
        }

        sb.append("\nREPLY FORMAT (one line per crop, same order as above). Use EXACT format:\n");
        sb.append("INDEX|CHOSEN:<category name or NONE>|SUGGESTION:<short name or NONE>\n");
        sb.append("Example:\n0|CHOSEN:VEGETABLE|SUGGESTION:NONE\n1|CHOSEN:NONE|SUGGESTION:HERB\n\n");
        sb.append("Now reply for the listed crops:\n");

        String prompt = sb.toString();

        String aiText;
        try {
            ChatResponse response = chatClient
                    .prompt(prompt)
                    .call()
                    .chatResponse();

            aiText = response.getResult().getOutput().getText();
        } catch (Exception ex) {
            // logger.warn("AI batch failure: {}", ex.getMessage());
            // On failure return an "empty" suggestion result for each item
            List<SuggestionResult> fallback = new ArrayList<>();
            for (int i = 0; i < cropNames.size(); i++) fallback.add(SuggestionResult.empty());
            return fallback;
        }

        // parse aiText lines
        String[] lines = aiText == null ? new String[0] : aiText.split("\\r?\\n");
        // Initialize defaults (one per crop)
        List<SuggestionResult> results = new ArrayList<>();
        for (int i = 0; i < cropNames.size(); i++) results.add(SuggestionResult.empty());

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isBlank()) continue;
            // expect: INDEX|CHOSEN:...|SUGGESTION:...
            String[] parts = line.split("\\|");
            if (parts.length < 3) continue;
            try {
                int idx = Integer.parseInt(parts[0].trim());
                String chosenPart = null;
                String suggestionPart = null;
                for (int p = 1; p < parts.length; p++) {
                    String part = parts[p].trim();
                    if (part.toUpperCase().startsWith("CHOSEN:")) {
                        chosenPart = part.substring("CHOSEN:".length()).trim();
                    } else if (part.toUpperCase().startsWith("SUGGESTION:")) {
                        suggestionPart = part.substring("SUGGESTION:".length()).trim();
                    }
                }
                if (idx >= 0 && idx < cropNames.size()) {
                    String matchedExisting = null;
                    String suggestion = null;
                    if (chosenPart != null && !chosenPart.isBlank() && !chosenPart.equalsIgnoreCase("NONE")) {
                        // chosenPart may be an existing category or something else.
                        matchedExisting = chosenPart;
                    }
                    if (suggestionPart != null && !suggestionPart.isBlank() && !suggestionPart.equalsIgnoreCase("NONE")) {
                        suggestion = suggestionPart;
                    }
                    // write into results[idx]
                    results.set(idx, SuggestionResult.of(matchedExisting, suggestion));
                }
            } catch (NumberFormatException ignore) {
                // ignore malformed lines
            }
        }

        // Final normalization step:
        // If the chosen value is present but doesn't match case-insensitively any existingCategory, we
        // leave matchedExisting as the raw chosen (caller will map to actual existing if it matches).
        return results;
    }

    public static final class SuggestionResult {
        public final String matchedExisting; // raw string returned in CHOSEN (may or may not match existingCategories)
        public final String suggestion;       // suggested new category or null

        private SuggestionResult(String matchedExisting, String suggestion) {
            this.matchedExisting = matchedExisting;
            this.suggestion = suggestion;
        }

        public static SuggestionResult of(String matchedExisting, String suggestion) {
            return new SuggestionResult(matchedExisting, suggestion);
        }

        public static SuggestionResult empty() {
            return new SuggestionResult(null, null);
        }

        public boolean hasAny() {
            return matchedExisting != null || suggestion != null;
        }
    }
}
