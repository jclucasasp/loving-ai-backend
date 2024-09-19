package com.example.ai_dating_backend.services.interfaces;
import com.example.ai_dating_backend.responses.Response;

import java.util.concurrent.CompletableFuture;

public interface ResponseServiceInterface {
    CompletableFuture<String> generateChatResponse(Response res);
}
