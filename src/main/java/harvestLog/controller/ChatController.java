    package harvestLog.controller;

    import harvestLog.model.Farmer;
    import harvestLog.model.PlanType;
    import harvestLog.service.PlanService;
    import harvestLog.service.RateLimitResult;
    import harvestLog.service.RateLimitService;
    import harvestLog.service.ai.CategoryAiService;
    import harvestLog.service.ai.CategoryAiToolService;
    import harvestLog.service.ai.CropAiToolService;
    import harvestLog.service.ai.FieldAiToolService;
    import harvestLog.service.ai.HarvestRecordAiToolService;
    import harvestLog.service.ai.MeasureUnitAiToolService;
    import harvestLog.service.impl.FarmerService;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.ai.chat.model.ChatResponse;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import java.time.LocalDate;
    import java.util.List;

    import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;
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
        private final FarmerService farmerService;
        private final PlanService planService;
        private final RateLimitService rateLimitService;

        public ChatController(@Qualifier("chatClient") ChatClient chatClient, CategoryAiService categoryAiService,
                              HarvestRecordAiToolService harvestRecordAiToolService, CropAiToolService cropAiToolService,
                              FieldAiToolService fieldAiToolService, CategoryAiToolService categoryAiToolService,
                              MeasureUnitAiToolService measureUnitAiToolService,
                              FarmerService farmerService, PlanService planService,
                              RateLimitService rateLimitService) {
            this.chatClient = chatClient;
            this.categoryAiService = categoryAiService;
            this.harvestRecordAiToolService = harvestRecordAiToolService;
            this.cropAiToolService = cropAiToolService;
            this.fieldAiToolService = fieldAiToolService;
            this.categoryAiToolService = categoryAiToolService;
            this.measureUnitAiToolService = measureUnitAiToolService;
            this.farmerService = farmerService;
            this.planService = planService;
            this.rateLimitService = rateLimitService;
        }

        @GetMapping("/test-category-ai")
        public List<CategoryAiService.SuggestionResult> testCategoryAi(
                @RequestParam List<String> crops,
                @RequestParam(required = false, defaultValue = "") List<String> existing) {
            return categoryAiService.suggestForBatch(crops, existing);
        }

        @GetMapping
        public ResponseEntity<String> getResponse(@RequestParam String prompt, @RequestParam String chatId) {
            Long farmerId = getAuthenticatedFarmerId();
            Farmer farmer = farmerService.findById(farmerId);
            if (planService.getEffectivePlan(farmer) == PlanType.FREE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"code\":\"PREMIUM_REQUIRED\",\"message\":\"The AI Assistant is a FARM plan feature.\"}");
            }

            RateLimitResult rateLimit = rateLimitService.check(farmerId);
            if (!rateLimit.allowed()) {
                String body = String.format(
                        "{\"code\":\"RATE_LIMITED\",\"reason\":\"%s\",\"resetAt\":\"%s\"}",
                        rateLimit.reason(), rateLimit.resetAt());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            }

            ChatResponse response = chatClient
                    .prompt()
                    .system(s -> s.param("currentDate", LocalDate.now().toString()))
                    .user(prompt)
                    .tools(harvestRecordAiToolService, cropAiToolService, fieldAiToolService, categoryAiToolService, measureUnitAiToolService)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .call()
                    .chatResponse();

            return ResponseEntity.ok(response.getResult().getOutput().getText());
        }
    }