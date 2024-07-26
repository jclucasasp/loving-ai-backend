package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.profile.Profile;

import java.util.List;

public record Conversation(
        String id,
        String profileId,
        List<ChatMessage> messages
) {
}
