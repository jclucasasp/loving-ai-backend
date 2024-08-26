package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

public record Conversation(
        @Indexed(name = "Conversation-id-idx")
        String matchedId,
        List<ChatMessage> messages
) {
}
