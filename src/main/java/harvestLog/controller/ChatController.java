    package harvestLog.controller;

    import harvestLog.service.HarvestRecordAiToolService;
    import harvestLog.service.HarvestRecordService;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.ai.chat.model.ChatResponse;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

    @RestController
    @RequestMapping("/api/ai")
    public class ChatController {

        @Autowired
        private ChatClient chatClient;
        @Autowired
        private HarvestRecordAiToolService harvestRecordAiToolService;


        @GetMapping
        public String getResponse(@RequestParam String prompt, @RequestParam String chatId) {
            System.out.println("prompted with: " + prompt + " and id: " + chatId);
            ChatResponse response = chatClient
                    .prompt(prompt)
                    .tools(harvestRecordAiToolService)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .call()
                    .chatResponse();



            return response.getResult().getOutput().getText();
        }
    }