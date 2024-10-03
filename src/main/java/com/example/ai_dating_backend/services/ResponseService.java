package com.example.ai_dating_backend.services;

import com.example.ai_dating_backend.responses.Response;
import com.example.ai_dating_backend.services.interfaces.ResponseServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

//TODO: Every chat needs a new Chat memory, else the ai uses it to save all the conversations. Prop have to use a vector
// or redis to save with userId. Already changed the front and backend Response to include it
// Or use a Vector db to store the messages in.

@Log4j2
@Service
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class ResponseService implements ResponseServiceInterface {

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final PromptGenerator promptGenerator;
    private final ChatClient chatClient;

    private Map<String, String> prompt = new ConcurrentHashMap<>();
    private final Map<String, ChatMemory> userChatMemories = new ConcurrentHashMap<>();

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res) {

        return CompletableFuture.supplyAsync(() -> {
            prompt = promptGenerator.generatedChatPrompt(res);

            String userId = res.userId();
            ChatMemory chatMemory = getUserSpecificMemory(userId);

            log.info("\nGenerating chat response for user [{}] on thread {}", userId, Thread.currentThread().getName());

            try {
                log.info("Generating prompt with prompt: {} ", prompt);

                return chatClient
                        .prompt()
                        .system(prompt.get("system"))
                        .user(prompt.get("user"))
                        .advisors(new MessageChatMemoryAdvisor(chatMemory))
                        .call()
                        .content();
            } catch (Exception e) {
                log.error("Error generating chat response: ", e);
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }

    @Override
    public void setChatMemory(String userId, ChatMemory chatMemory) {
        userChatMemories.put(userId, chatMemory);
    }

    @Override
    public ChatMemory getChatMemory(String userId) {
        return userChatMemories.getOrDefault(userId, new InMemoryChatMemory());
    }

    @Override
    public ChatMemory getUserSpecificMemory(String userId) {
        ChatMemory chatMemory = getChatMemory(userId);
        setChatMemory(userId, chatMemory);
        return chatMemory;
    }
}
