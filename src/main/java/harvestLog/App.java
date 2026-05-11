package harvestLog;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

import io.github.cdimascio.dotenv.Dotenv;


@EnableAspectJAutoProxy
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class App {


    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String[] keys = {"DB_URL", "DB_USERNAME", "DB_PASSWORD", "OPENAI_API_KEY", "MAIL_USER", "MAIL_PASS", "RESEND_API_KEY", "MAIL_FROM", "FRONTEND_URL"};
        for (String key : keys) {
            String value = dotenv.get(key);
            if (value != null) System.setProperty(key, value);
        }

        SpringApplication.run(App.class, args);
    }

//    @Bean
//    public CommandLineRunner demo() {
//        return args -> {
//            System.out.println(">>> Application Started");
//        };
//    }

    static final String SYSTEM_PROMPT =
            "You are a farm management assistant. Today's date is {currentDate}.\n\n" +
            "Rules:\n" +
            "1. Never ask the user for IDs. Always resolve names (crops, fields, categories, measure units) to IDs yourself using the available list tools before acting.\n" +
            "2. Before any write operation (create, update, delete), send ONE message that: (a) lists everything you will create or modify, including any side-effect entities (e.g. a new MeasureUnit or Category that does not yet exist), and (b) asks about any genuinely missing required fields. Execute immediately once the user responds — no second confirmation.\n" +
            "3. Focus on farm management. Politely decline clearly unrelated requests; farming-adjacent questions (weather, pricing, agronomy) are welcome.";

    @Bean
    @Qualifier("chatClient")
    public ChatClient chatClient(ChatModel chatModel) {
        Advisor memory = new MessageChatMemoryAdvisor(new InMemoryChatMemory());
        return ChatClient.builder(chatModel)
                .defaultAdvisors(memory)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Bean
    @Qualifier("plainChatClient")
    public ChatClient plainChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

//    @Bean
//    public List<ToolS> toolSpecifications(HarvestRecordAiToolService harvestRecordAiToolService) {
//        return ToolSpecification.createFrom(harvestRecordAiToolService);
//    }


}
