package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

public record Conversation(
        String id,
        @Indexed(name = "fromProfileId-idx")
        String fromProfileId,
        @Indexed(name = "toProfileId-idx")
        String toProfileId,
        List<ChatMessage> messages
) {
}
