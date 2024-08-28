package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

public record Conversation(
        @Indexed(name = "Conversation-id-idx")
        @MongoId(FieldType.STRING)
        String matchId,
        List<ChatMessage> messages
) {
}
