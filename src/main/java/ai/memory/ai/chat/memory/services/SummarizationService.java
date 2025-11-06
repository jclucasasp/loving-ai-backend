package ai.memory.ai.chat.memory.services;

import ai.memory.ai.chat.memory.conversations.ChatMessage;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Objects;

public class SummarizationService {
    private final ChatClient chatClient;

    public SummarizationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String summarizeOlderMessages(String olderChatText) {
        String prompt = """
                Summarize the following conversation in 2–3 concise sentences.
                Preserve tone, key facts, and emotional context.
                Do not add new information.
                
                Conversation:
                """ + olderChatText;

        return Objects.requireNonNull(chatClient.prompt().user(prompt).call().content()).trim();
    }

    public String formatMessagesForSummary(List<ChatMessage> messages, String currentUserProfileId) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            sb.append((message.isUserMessage(currentUserProfileId) ? "User: " : "Assistant: ")).append(message.content()).append("\n");
        }
        return sb.toString();
    }
}
