package com.example.tinder_ai_backend.session;

import java.util.Date;
import java.util.UUID;

public record UserSession(String sessionId, String userId, Date logInDate, Date logOutDate) {

    public UserSession(String userId, Date logInDate, Date logOutDate) {
        this(UUID.randomUUID().toString(), userId, logInDate, logOutDate);
    }
}
