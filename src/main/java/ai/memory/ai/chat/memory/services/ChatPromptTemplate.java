package ai.memory.ai.chat.memory.services;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class ChatPromptTemplate {
    private final String systemMessage;
    private final String userMessage;

    public Map<String, String> getPrompt() {
        return Map.of(
                "system", systemMessage,
                "user", userMessage
        );
    }
}
