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

import io.github.cdimascio.dotenv.Dotenv;


@EnableAspectJAutoProxy
@SpringBootApplication
@EnableScheduling
public class App {


    public static void main(String[] args) {
         Dotenv dotenv = Dotenv.load();

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
         System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
        System.setProperty("MAIL_USER", dotenv.get("MAIL_USER"));
        System.setProperty("MAIL_PASS", dotenv.get("MAIL_PASS"));

        SpringApplication.run(App.class, args);
    }

//    @Bean
//    public CommandLineRunner demo() {
//        return args -> {
//            System.out.println(">>> Application Started");
//        };
//    }

    String[] systemPrompts = new String[] {
            "You are an assistant for a farm management system. \" +\n" +
                    "                        \"You help users manage crops, fields, and harvest entries. \" +\n" +
                    "                        \"Assist them in adding or querying information. \" +\n" +
                    "                        \"Be precise and always confirm actions.",
            "You are an assistant for a farm management system. " +
                    "You help users (farmers) manage crops, fields, and harvest entries. " +
                    "When a user requests to fetch or retrieve harvest records without filters, use the `getAllHarvestRecords` tool directly. " +
                    "When a user requests harvest records with specific filters (e.g., field IDs, crop IDs, or dates), use the `getFiltered` tool directly. " +
                    "For actions like creating, updating, or deleting records, confirm with the user before proceeding. " +
                    "Be precise in your responses."
    };

    @Bean
    @Qualifier("chatClient")
    public ChatClient chatClient(ChatModel chatModel) {
        Advisor memory = new MessageChatMemoryAdvisor(new InMemoryChatMemory());
        return ChatClient.builder(chatModel)
                .defaultAdvisors(memory)
                .defaultSystem(systemPrompts[1])
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
