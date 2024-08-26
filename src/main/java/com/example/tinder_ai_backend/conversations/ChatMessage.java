package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

public record ChatMessage(
        String id,
        @Indexed(name = "ChatMessage-id-idx")
        String conversationId,
        @Indexed(name = "SenderProfile-id-idx")
        String senderProfileId,
        @Indexed(name = "ReceiverProfile-id-idx")
        String receiverProfileId,
        @Indexed(name = "ChatMessage-createDate-idx")
        Date sendDate,
        String messageText
) {}
