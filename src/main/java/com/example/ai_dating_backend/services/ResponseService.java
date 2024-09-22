package com.example.ai_dating_backend.services;

import com.example.ai_dating_backend.responses.Response;
import com.example.ai_dating_backend.services.interfaces.ResponseServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

//TODO: Look at the Messages class from ChatMemory. You can change this to use the db to look up messages.
// Or use a Vector db to store the messages in as it looses its mind after generating too many responses.

@Log4j2
@Service
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class ResponseService implements ResponseServiceInterface {

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final PromptGenerator promptGenerator;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res) {
        log.info("\nGenerating chat response on thread {}", Thread.currentThread().getName());

        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> prompt = promptGenerator.generatedChatPrompt(res);

            try {

                log.info("Generating prompt with prompt: {} ", prompt);

                return chatClient
                        .prompt()
                        .system(prompt.get("system"))
//                        .system(sp -> sp.param("extraConfig", prompt.get("system")))
                        .user(prompt.get("user"))
                        .advisors(new PromptChatMemoryAdvisor(chatMemory))
                        .call()
                        .content();
            } catch (Exception e) {
                log.error("Error generating chat response: ", e);
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }
}
