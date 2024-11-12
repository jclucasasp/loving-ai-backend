package com.example.ai_dating_backend.services;

import com.example.ai_dating_backend.responses.Response;
import com.example.ai_dating_backend.services.interfaces.ResponseServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
    private final OpenAiChatModel chatModel;

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res, String matchId) {

        return CompletableFuture.supplyAsync(() -> {
            prompt = promptGenerator.generatedChatPrompt(res);

            ChatMemory chatMemory = getUserSpecificMemory(matchId);

            log.info("\nGenerating chat response for user [{}] on thread {}", res.name(), Thread.currentThread().getName());

            try {
                log.info("Generating prompt with prompt: {} ", prompt);
//              Use the chatClient when running a local LLM

//                return chatClient
//                        .prompt()
//                        .system(prompt.get("system"))
//                        .user(prompt.get("user"))
//                        .advisors(new MessageChatMemoryAdvisor(chatMemory))
//                        .call()
//                        .content();

//              This is for running an OpenAi LLM
                Message userMessage = new UserMessage(prompt.get("user"));
                SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(prompt.get("system"));

                Prompt prompt1 = new Prompt(String.valueOf(List.of(userMessage, systemPromptTemplate.getTemplate())));
                return chatModel.call(prompt1).getResult().getOutput().getContent();
            } catch (Exception e) {
                log.error("Error generating chat response: ", e);
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }

    @Override
    public void setChatMemory(String chatId, ChatMemory chatMemory) {
        userChatMemories.put(chatId, chatMemory);
    }

    @Override
    public ChatMemory getChatMemory(String chatId) {
        return userChatMemories.getOrDefault(chatId, new InMemoryChatMemory());
    }

    @Override
    public ChatMemory getUserSpecificMemory(String chatId) {
        ChatMemory chatMemory = getChatMemory(chatId);
        setChatMemory(chatId, chatMemory);
        return chatMemory;
    }
}
