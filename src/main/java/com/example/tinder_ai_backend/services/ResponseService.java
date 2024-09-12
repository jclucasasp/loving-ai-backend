package com.example.tinder_ai_backend.services;

import com.example.tinder_ai_backend.responses.Response;
import com.example.tinder_ai_backend.services.interfaces.ResponseServiceInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class ResponseService implements ResponseServiceInterface {

    private static final Logger logger = LogManager.getLogger(ResponseService.class);

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res) {
        logger.info("\nGenerating chat response on thread {}", Thread.currentThread().getName());

        final String EXTRA_CONFIG = "You are a " + res.age() + " years old single " + res.gender() + " of " + res.ethnicity() + " on a dating app, and your name is " + res.name() + "." +
                "You're bio is:  " + res.bio();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return chatClient
                        .prompt()
                        .system(sp -> sp.param("extraConfig", EXTRA_CONFIG))
                        .user(res.messagePrompt())
                        .advisors(new PromptChatMemoryAdvisor(chatMemory))
                        .call()
                        .content();
            } catch (Exception e) {
                logger.error("Error generating chat response: ", e);
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }
}
