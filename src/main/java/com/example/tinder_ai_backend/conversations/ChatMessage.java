package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

public record ChatMessage(
        @Indexed(name = "ChatMessage-id-idx")
        String id,
        @Indexed(name = "ChatMessage-createDate-idx")
        Date createDate,
        String messageText
) {
}
