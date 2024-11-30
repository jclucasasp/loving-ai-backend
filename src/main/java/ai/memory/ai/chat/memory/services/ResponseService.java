package ai.memory.ai.chat.memory.services;

import ai.memory.ai.chat.memory.config.ChatClientConfig;
import ai.memory.ai.chat.memory.services.interfaces.ResponseServiceInterface;
import ai.memory.ai.chat.memory.responses.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

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
    private final ChatClientConfig clientConfig;
    private final ChatClient chatClient;

    private Map<String, String> prompt = new ConcurrentHashMap<>();
    private final Map<String, ChatMemory> userChatMemories = new ConcurrentHashMap<>();

    @Async("chatResponseExecutor")
    @Override
    // to implement streaming, just change to CompletableFuture<Flux><String>>
    public CompletableFuture<String> generateChatResponse(Response res, String matchId) {

        return CompletableFuture.supplyAsync(() -> {
            prompt = promptGenerator.generatedChatPrompt(res);

            ChatMemory chatMemory = getUserSpecificMemory(matchId);

            log.info("\nGenerating chat response for user [{}] on thread {}", res.name(), Thread.currentThread().getName());

            try {
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
