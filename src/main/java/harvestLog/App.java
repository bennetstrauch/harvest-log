package harvestLog;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//    @Bean
//    public CommandLineRunner demo() {
//        return args -> {
//            System.out.println(">>> Application Started");
//        };
//    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
//change to db for production #
        Advisor memory = new MessageChatMemoryAdvisor(new InMemoryChatMemory());

        return ChatClient.builder(chatModel)
                .defaultAdvisors(memory)
                .defaultSystem("You are an assistant for a farm management system. " +
                        "You help users manage crops, fields, and harvest entries. " +
                        "Assist them in adding or querying information. " +
                        "Be precise and always confirm actions.")
                .build();
    }

}
