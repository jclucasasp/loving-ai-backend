package com.example.tinder_ai_backend.services.interfaces;

import com.example.tinder_ai_backend.responses.Response;

import java.util.concurrent.CompletableFuture;

public interface ResponseServiceInterface {
    CompletableFuture<String> generateChatResponse(Response res);
}
