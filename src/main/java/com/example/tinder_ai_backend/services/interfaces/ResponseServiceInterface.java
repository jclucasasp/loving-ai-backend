package com.example.tinder_ai_backend.services.interfaces;

import com.example.tinder_ai_backend.responses.Response;

import java.util.concurrent.Future;

public interface ResponseServiceInterface {
    Future<String> generateChatResponse(Response res);
}
