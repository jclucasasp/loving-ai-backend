package com.example.tinder_ai_backend.user;

import com.example.tinder_ai_backend.profile.Gender;

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
        String myersBriggsPersonalityType
) {}
