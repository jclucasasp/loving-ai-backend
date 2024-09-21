package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Gender;
import com.mongodb.lang.NonNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

public record NewUser (
        @Min(value = 2)
        @NonNull
        String firstName,
        @NonNull
        @Min(value = 2)
        String lastName,
        @NonNull
        @Email
        String email,
        @NonNull
        @Min(value = 8)
        String password,
        @NonNull
        int age,
        @NonNull
        String ethnicity,
        @NonNull
        Gender gender,
        @NonNull
        String bio,
        String imageUrl,
        @NonNull
        String personalityTypeId
) {}
