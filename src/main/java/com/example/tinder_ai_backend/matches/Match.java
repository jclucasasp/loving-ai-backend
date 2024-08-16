package com.example.tinder_ai_backend.matches;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

public record Match(
        String id,
        Date createDate,
        @Indexed(name = "MatchFromProfileId-idx")
        String fromProfileId,
        String toProfileId,
        String conversationId
){}
