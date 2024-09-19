package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Gender;

public record NewUser (
        String firstName,
        String lastName,
        String email,
        String password,
        int age,
        String ethnicity,
        Gender gender,
        String bio,
        String imageUrl,
        String personalityTypeId
) {}
