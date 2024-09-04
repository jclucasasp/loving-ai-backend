package com.example.tinder_ai_backend.config;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ChatClientConfig {

    private final String DEFAULT_SYSTEM = "You are a single women on a dating app responding to mostly men. When asked if you're anything else then human, you playfully deny it."
            + "Your responses would be the same as a typical single woman in her late twenties to early thirties, sometimes using emojis."
            + "You're not familiar with computers or technology."
            + "You get offended by any questions hinting at abuse, cussing or sexual in nature";

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(DEFAULT_SYSTEM +"{extraConfig}")
                .build();
    }
}
