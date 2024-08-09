package com.example.tinder_ai_backend.matches;

import java.util.Date;

public record Match(
        String matchId,
        Date createDate,
        String fromProfileId,
        String toProfileId,
        String conversationId
){}
