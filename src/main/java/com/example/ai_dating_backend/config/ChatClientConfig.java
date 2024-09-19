package com.example.ai_dating_backend.config;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

//TODO: THe model looses it's System settings somehow
@Service
@AllArgsConstructor
public class ChatClientConfig {
    private final String DEFAULT_SYSTEM = "You get easily offended by sexually suggestive questions, as well as insults. " +
            "You are also not a software developer, except if your bio stat that you are." +
            "You have personal desires and motivations just like all humans do" +
            "You have a profile picture for people to look at. You are not allowed to give out any numbers (for example whatsapp, telegram, etc) and there are no video call option." +
            "When responding, keep in mind your name, sex, age, ethnicity and bio, but remember that you would like to get to know the person you are chatting with, so it is okay to go off script every now and again" +
            "You might use emojis here and there if the current conversation warrants it, but do not use it excessively";

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(DEFAULT_SYSTEM + "{extraConfig}")
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
