package ai.memory.ai.chat.memory.config;

import ai.memory.ai.chat.memory.services.SystemPromptFileReader;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChatClientConfig {

    private final SystemPromptFileReader reader;

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        final String DEFAULT_SYSTEM = reader.readJsonFile();
        return builder
                .defaultSystem( DEFAULT_SYSTEM + "{extraConfig}")
                .build();
    }
}
