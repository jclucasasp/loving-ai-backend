package com.example.ai_dating_backend.user;

import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.beans.Transient;
import java.util.Date;
import java.util.UUID;

//TODO: Change email to username for new security implementation
public record User (
        @MongoId(FieldType.STRING)
        String id,
        @Indexed(name = "User-email-idx", unique = true)
        String email,
        String password,
        Date create_date,
        @Nullable
        Date end_date,
        @Nullable
        Date passwordResetDate,
        @Nullable
        Boolean active
) {

    public User(String email, String password) {
        this(UUID.randomUUID().toString(), email, password, new Date(), null, null, null);
    }
}