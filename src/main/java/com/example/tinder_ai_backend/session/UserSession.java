package com.example.tinder_ai_backend.session;

import java.util.Date;

public record UserSession(String sessionId, String userId, Date loggedIn) {
}
