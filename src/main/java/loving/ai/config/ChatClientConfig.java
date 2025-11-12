package loving.ai.config;

import loving.ai.services.SummarizationService;
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
