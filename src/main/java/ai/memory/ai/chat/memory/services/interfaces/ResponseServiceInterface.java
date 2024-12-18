package ai.memory.ai.chat.memory.services.interfaces;
import ai.memory.ai.chat.memory.responses.Response;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.concurrent.CompletableFuture;

public interface ResponseServiceInterface {
    CompletableFuture<String> generateChatResponse(Response res, String matchId);
//    void setChatMemory(String userId, ChatMemory chatMemory);
//    ChatMemory getChatMemory(String userId);
//    ChatMemory getUserSpecificMemory(String userId);
}
