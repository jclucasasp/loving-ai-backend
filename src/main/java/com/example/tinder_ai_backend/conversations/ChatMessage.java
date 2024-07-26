package com.example.tinder_ai_backend.conversations;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessage(
        String messageText,
        String profileId,
        LocalDateTime messageTime
) {
}
