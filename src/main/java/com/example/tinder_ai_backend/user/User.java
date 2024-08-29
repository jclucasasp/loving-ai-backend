package com.example.tinder_ai_backend.user;

import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.UUID;

public record User(
        @MongoId(FieldType.STRING)
        String id,
        @Indexed(name = "User-email-idx", unique = true)
        String email,
        String password,
        Date create_date,
        Boolean active,
        @Nullable
        Date end_date,
        @Nullable
        Date passwordResetDate
) {
    public User(
            String id,
            String email, String password, Date create_date, Boolean active, @Nullable
            Date end_date, @Nullable
            Date passwordResetDate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.create_date = create_date;
        this.active = active;
        this.end_date = end_date;
        this.passwordResetDate = passwordResetDate;
    }

    public User(String email, String password) {
        this(UUID.randomUUID().toString(), email, password, new Date(), false, null, null);
    }
}
