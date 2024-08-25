package com.example.tinder_ai_backend.user;

import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;
import java.util.UUID;

public record User
        (String id,
         @Nullable
         String sessionId,
         String profileId,
         @Indexed(name = "User-email-idx", unique = true)
         String email,
         String password,
         Date create_date,
         @Nullable
         Date end_date,
         @Nullable
         Date passwordResetDate) {

    public User(String email, String password) {
        this(UUID.randomUUID().toString(), null, null, email, password, new Date(), null, null);
    }
}
