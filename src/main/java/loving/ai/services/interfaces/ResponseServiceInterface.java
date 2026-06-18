package loving.ai.services.interfaces;
import loving.ai.dto.responses.Response;

import java.util.concurrent.CompletableFuture;

public interface ResponseServiceInterface {
    CompletableFuture<String> generateChatResponse(Response res, String matchId);
//    void setChatMemory(String userId, ChatMemory chatMemory);
//    ChatMemory getChatMemory(String userId);
//    ChatMemory getUserSpecificMemory(String userId);
}
