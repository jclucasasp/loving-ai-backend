package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

public record ChatMessage(
        String messageText,
        @Indexed(name="toProfile-idx")
        String toProfile,
        @Indexed(name = "messageTime-idx")
        LocalDateTime messageTime
) {
}
