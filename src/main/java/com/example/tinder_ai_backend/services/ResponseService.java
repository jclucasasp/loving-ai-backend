package com.example.tinder_ai_backend.services;

import com.example.tinder_ai_backend.responses.Response;
import com.example.tinder_ai_backend.services.interfaces.ResponseServiceInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
    private final VectorStore vectorStore;

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res) {
        logger.info("\nGenerating chat response on thread {}", Thread.currentThread().getName());

        final String EXTRA_CONFIG = "Your name is " + res.name() + " and you are " + res.age() + " years old and you like the following: " + res.bio();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return chatClient
                        .prompt()
                        .system(sp -> sp.param("extraConfig", EXTRA_CONFIG))
                        .user(res.messagePrompt())
                        .advisors(new PromptChatMemoryAdvisor(chatMemory))
                        .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                        .call()
                        .content();
            } catch (Exception e) {
                logger.error("Error generating chat response: ", e);
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }
}
