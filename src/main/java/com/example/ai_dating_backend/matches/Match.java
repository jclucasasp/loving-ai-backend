package com.example.ai_dating_backend.matches;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

public record Match(
        @MongoId(FieldType.STRING)
        String id,
        Date matchedDate,
        @Indexed(name = "MatchFromProfileId-idx")
        String profileId,
        String toProfileId
){}
