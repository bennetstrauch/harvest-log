//package harvestLog.service.ai;
//
//import harvestLog.model.Category;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class CropCategoryAIService {
//
//    private final OpenAiClient client;
//
//    public CropCategoryAIService(OpenAiClient client) {
//        this.client = client;
//    }
//
//    public Map<String, Category> suggestCategories(List<String> cropNames) {
//        String categoriesList = Arrays.stream(Category.values())
//                .map(Enum::name)
//                .collect(Collectors.joining(", "));
//
//        String cropsList = cropNames.stream()
//                .map(name -> "- " + name)
//                .collect(Collectors.joining("\n"));
//
//        String prompt = """
//Classify the following crop names into one of the categories: [%s].
//
//Respond with a JSON object like:
//{ "carrot": "VEGETABLE", "kumquat": "FRUIT", ... }
//
//If you’re not completely sure, guess the most likely category.
//Only return "UNKNOWN" if you truly have no idea.
//
//Crop list:
//%s
//""".formatted(categoriesList, cropsList);
//
//
//        ChatResponse response = client.call(ChatRequest.builder()
//                .messages(List.of(new SystemMessage(prompt)))
//                .build());
//
//        String content = response.getResult().getOutput().getContent();
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, String> raw = mapper.readValue(content, new TypeReference<>() {});
//            Map<String, Category> result = new HashMap<>();
//
//            for (Map.Entry<String, String> entry : raw.entrySet()) {
//                String name = entry.getKey().trim();
//                String cat = entry.getValue().trim().toUpperCase();
//                if ("UNKNOWN".equals(cat)) {
//                    result.put(name, null);
//                } else {
//                    try {
//                        result.put(name, Category.valueOf(cat));
//                    } catch (IllegalArgumentException e) {
//                        result.put(name, null);
//                    }
//                }
//            }
//            return result;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse AI response: " + content, e);
//        }
//    }
//}
