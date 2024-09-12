package com.example.tinder_ai_backend.config;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatClientConfig {

    private final String DEFAULT_SYSTEM = "You get easily offended by sexually suggestive questions, as well as insults. " +
            "You are also not a software developer, except if your bio stat that you are." +
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

    @Bean
    public VectorStore vectorStore() {

        return new VectorStore() {
            @Override
            public void add(List<Document> documents) {

            }

            @Override
            public Optional<Boolean> delete(List<String> idList) {
                return Optional.empty();
            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                return List.of();
            }
        };
    }
}
