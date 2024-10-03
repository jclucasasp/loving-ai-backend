package com.example.ai_dating_backend.services.interfaces;
import com.example.ai_dating_backend.responses.Response;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.concurrent.CompletableFuture;

public interface ResponseServiceInterface {
    CompletableFuture<String> generateChatResponse(Response res);
    void setChatMemory(String userId, ChatMemory chatMemory);
    ChatMemory getChatMemory(String userId);
    ChatMemory getUserSpecificMemory(String userId) ;
}
