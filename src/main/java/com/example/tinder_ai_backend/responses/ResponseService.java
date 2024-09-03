package com.example.tinder_ai_backend.responses;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@EnableAsync
public class ResponseService {

    private static final Map<String, String> WITTY_RESPONSES = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(ResponseService.class);
    static Pattern badPattern;

    static {
        WITTY_RESPONSES.put("areyoua(llm|ai|bot|chatbot|robot|human|alien)", "Lol, I'm sure not a bot!");
        WITTY_RESPONSES.put("areyou(evenhuman|evenaperson|llm|ai|bot|chatbot|robot|human|alien)", "What a weird question, lol!");
        WITTY_RESPONSES.put("amitalkingtoa(llm|ai|bot|chatbot|robot)", "No, you're just talking to yourself and it is only in your mind that you are seeing this message, lol!");
        WITTY_RESPONSES.put("willyoubemy(girlfriend|lover|wife)", "Cool your jets there Tex. I don't know you like that, lol");
        WITTY_RESPONSES.put("iloveyou\\b", "Of course you do, wink");
        badPattern = Pattern.compile("\\b(?:sex|intercourse|sodomy|kill|drown|electrocute|penetrate)\\b", Pattern.CASE_INSENSITIVE);
    }

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final OllamaChatModel chatClient;

    @Async("chatResponseExecutor")
    public Future<String> generateChatResponse(Prompt prompt) {
        logger.info("\nGenerating chat response on thread {}", Thread.currentThread().getName());
        Matcher badMatcher = badPattern.matcher(prompt.getContents());

        if (badMatcher.find()) {
            return CompletableFuture.completedFuture("You're such a weirdo...");
        }

        String normalisedInput = normaliseInput(prompt.getContents());

        for (Map.Entry<String, String> entry : WITTY_RESPONSES.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(normalisedInput).matches()) {
                return CompletableFuture.completedFuture(entry.getValue());
            }
        }
        return getResponse(prompt);
    }

    @Async("chatResponseExecutor")
    protected Future<String> getResponse(Prompt prompt) {
        return chatResponseExecutor.submit(() -> chatClient.call(prompt.getContents()));
    }

    private String normaliseInput(String input) {
        return input.toLowerCase().replaceAll("[\\s\\p{Punct}]", "");
    }
}
