package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Gender;
import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;

public record NewUser (
        @Nullable
        String userId,
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
        @Nullable
        String otp,
        @NonNull
        int age,
        @NonNull
        String ethnicity,
        @NonNull
        Gender gender,
        @NonNull
        String bio,
        @Transient
        MultipartFile image,
        String imageUrl,
        @NonNull
        String myersBriggsPersonalityType
) {}
