package ai.memory.ai.chat.memory.config;

import ai.memory.ai.chat.memory.services.SummarizationService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class ChatClientConfig {

    @Bean
    public SummarizationService summarizationService(ChatClient chatClient) {
        return new SummarizationService(chatClient);
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
