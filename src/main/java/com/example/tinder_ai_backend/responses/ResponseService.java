package com.example.tinder_ai_backend.responses;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ResponseService {

    private static final Map<String, String> WITTY_RESPONSES = new HashMap<>();
    static Pattern badPattern;

    static {
        WITTY_RESPONSES.put("areyoua(llm|ai|bot|chatbot|robot)", "Lol, I'm sure not a bot!");
        WITTY_RESPONSES.put("areyou(evenhuman|evenaperson)", "What a weird question, lol!");
        WITTY_RESPONSES.put("amitalkingtoa(llm|ai|bot|chatbot|robot)", "No, you're just talking to yourself and it is only in your mind that you are seeing this message, lol!");
        WITTY_RESPONSES.put("willyoubemy(girlfriend|lover|wife)", "Cool your jets there Tex. I don't know you like that, lol");
        WITTY_RESPONSES.put("iloveyou.*", "Of course you do, wink");
        badPattern = Pattern.compile("sex|intercourse|sodomy|kill|drown|electrocute|penetrate");
    }

    private final OllamaChatModel chatClient;

    public String getResponse(String input) {
        Matcher badMatcher = badPattern.matcher(input);

        if (badMatcher.find()) {
            return "You're such a weirdo...";
        }

        String normalisedInput = normaliseInput(input);

        for (Map.Entry<String, String> entry : WITTY_RESPONSES.entrySet()) {
            if (Pattern.matches(entry.getKey(), normalisedInput)) {
                return entry.getValue();
            }
        }

        Prompt prompt = new Prompt(input);
        ChatResponse chatResponse = chatClient.call(prompt);
        return chatResponse.getResult().getOutput().getContent();
    }

    private String normaliseInput(String input) {
        return input.toLowerCase().replaceAll("[\\s\\p{Punct}]", "");
    }

    public void printMap() {
        WITTY_RESPONSES.forEach(System.out::printf);
    }
}
