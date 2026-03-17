    package harvestLog.controller;

    import harvestLog.service.ai.CategoryAiService;
    import harvestLog.service.ai.CategoryAiToolService;
    import harvestLog.service.ai.CropAiToolService;
    import harvestLog.service.ai.FieldAiToolService;
    import harvestLog.service.ai.HarvestRecordAiToolService;
    import harvestLog.service.ai.MeasureUnitAiToolService;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.ai.chat.model.ChatResponse;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.List;

    import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

    @RestController
    @RequestMapping("/api/ai")
    public class ChatController {

        private final ChatClient chatClient;
        private final CategoryAiService categoryAiService;
        private final HarvestRecordAiToolService harvestRecordAiToolService;
        private final CropAiToolService cropAiToolService;
        private final FieldAiToolService fieldAiToolService;
        private final CategoryAiToolService categoryAiToolService;
        private final MeasureUnitAiToolService measureUnitAiToolService;

        public ChatController(@Qualifier("chatClient") ChatClient chatClient, CategoryAiService categoryAiService, HarvestRecordAiToolService harvestRecordAiToolService, CropAiToolService cropAiToolService, FieldAiToolService fieldAiToolService, CategoryAiToolService categoryAiToolService, MeasureUnitAiToolService measureUnitAiToolService) {
            this.chatClient = chatClient;
            this.categoryAiService = categoryAiService;
            this.harvestRecordAiToolService = harvestRecordAiToolService;
            this.cropAiToolService = cropAiToolService;
            this.fieldAiToolService = fieldAiToolService;
            this.categoryAiToolService = categoryAiToolService;
            this.measureUnitAiToolService = measureUnitAiToolService;
        }


        @GetMapping("/test-category-ai")
        public List<CategoryAiService.SuggestionResult> testCategoryAi(
                @RequestParam List<String> crops,
                @RequestParam(required = false, defaultValue = "") List<String> existing) {
            return categoryAiService.suggestForBatch(crops, existing);
        }

        @GetMapping
        public String getResponse(@RequestParam String prompt, @RequestParam String chatId) {
            System.out.println("prompted with: " + prompt + " and id: " + chatId);



            ChatResponse response = chatClient
                    .prompt(prompt)
                    .tools(harvestRecordAiToolService, cropAiToolService, fieldAiToolService, categoryAiToolService, measureUnitAiToolService)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .call()
                    .chatResponse();

            System.out.println("Response Metadata: " + response.getMetadata().toString());

            return response.getResult().getOutput().getText();
        }
    }