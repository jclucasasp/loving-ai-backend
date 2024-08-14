package com.example.tinder_ai_backend.matches;

import java.util.Date;

public record Match(
        String id,
        Date createDate,
        String fromProfileId,
        String toProfileId,
        String conversationId
){}
