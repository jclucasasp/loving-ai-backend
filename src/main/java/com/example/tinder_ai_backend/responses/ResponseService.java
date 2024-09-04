package com.example.tinder_ai_backend.responses;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@EnableAsync
public class ResponseService {

    private static final Logger logger = LogManager.getLogger(ResponseService.class);

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final ChatClient chatClient;

    @Async("chatResponseExecutor")
    public Future<String> generateChatResponse(String prompt) {
        logger.info("\nGenerating chat response on thread {}", Thread.currentThread().getName());
        return chatResponseExecutor.submit(() -> chatClient.prompt().user(prompt).call().content());
    }
}
