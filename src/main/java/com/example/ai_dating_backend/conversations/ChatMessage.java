package com.example.ai_dating_backend.conversations;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

public record ChatMessage(
        @Indexed(name = "ChatMessage-id-idx")
        @MongoId(FieldType.STRING)
        String id,
        @Indexed(name = "SenderProfile-id-idx")
        String senderProfileId,
        @Indexed(name = "ReceiverProfile-id-idx")
        String receiverProfileId,
        @Indexed(name = "ChatMessage-createDate-idx")
        Date sendDate,
        String messageText
) {
}
