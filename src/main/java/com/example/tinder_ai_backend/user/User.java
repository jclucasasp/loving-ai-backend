package com.example.tinder_ai_backend.user;

import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

public record User(
        String id,
        String profileId,
        @Indexed(name = "User-email-idx")
        String email,
        String password,
        Date create_date,
        @Nullable
        Date end_date,
        @Nullable
        Date passwordResetDate,
        @Nullable
        String matchId
) {
}
