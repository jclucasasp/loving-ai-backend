package com.example.tinder_ai_backend.matches;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

public record Match(
        String id,
        Date matchedDate,
        @Indexed(name = "MatchFromProfileId-idx")
        String profileId,
        String toProfileId
){}
