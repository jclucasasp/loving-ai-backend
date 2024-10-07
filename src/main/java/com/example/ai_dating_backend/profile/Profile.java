package com.example.ai_dating_backend.profile;


import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

public record Profile(
        @Indexed(name = "ProfileId-idx")
        @MongoId(FieldType.STRING)
        String userId,
        String firstName,
        String lastName,
        int age,
        String ethnicity,
        Gender gender,
        String bio,
        String imageUrl,
        boolean ai,
        String myersBriggsPersonalityType
) {
}
