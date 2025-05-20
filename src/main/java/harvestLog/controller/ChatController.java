    package harvestLog.controller;

//    import harvestLog.service.CropAiToolService;
    import harvestLog.service.CropAiToolService;
    import harvestLog.service.HarvestRecordAiToolService;
    import harvestLog.service.HarvestRecordService;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.ai.chat.model.ChatResponse;
    import org.springframework.beans.factory.annotation.Autowired;
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
        private final HarvestRecordAiToolService harvestRecordAiToolService;
        private final CropAiToolService cropAiToolService;

        public ChatController(ChatClient chatClient, HarvestRecordAiToolService harvestRecordAiToolService, CropAiToolService cropAiToolService) {
            this.chatClient = chatClient;
            this.harvestRecordAiToolService = harvestRecordAiToolService;
            this.cropAiToolService = cropAiToolService;
        }


        @GetMapping
        public String getResponse(@RequestParam String prompt, @RequestParam String chatId) {
            System.out.println("prompted with: " + prompt + " and id: " + chatId);



            ChatResponse response = chatClient
                    .prompt(prompt)
                    .tools(harvestRecordAiToolService, cropAiToolService)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .call()
                    .chatResponse();

            System.out.println("Response Metadata: " + response.getMetadata().toString());

            return response.getResult().getOutput().getText();
        }
    }