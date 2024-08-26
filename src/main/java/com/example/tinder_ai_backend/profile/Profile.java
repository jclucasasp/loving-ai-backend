package com.example.tinder_ai_backend.profile;


import org.springframework.data.mongodb.core.index.Indexed;

public record Profile(
        @Indexed(name = "ProfileId-idx")
        String userId,
        String firstName,
        String lastName,
        int age,
        String ethnicity,
        Gender gender,
        String bio,
        String imageUrl,
        String myersBriggsPersonalityType
) {
}
