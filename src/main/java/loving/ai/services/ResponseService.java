package loving.ai.services;

import loving.ai.conversations.ChatMessage;
import loving.ai.conversations.Conversation;
import loving.ai.conversations.ConversationRepo;
import loving.ai.services.interfaces.ResponseServiceInterface;
import loving.ai.responses.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class ResponseService implements ResponseServiceInterface {

    private final ThreadPoolTaskExecutor chatResponseExecutor;
    private final PromptGenerator promptGenerator;
    private final ConversationRepo conversationRepo;
    private final SummarizationService summarizationService;
    private final ChatClient chatClient;

    private final static int MAX_MESSAGES = 20;

    private Map<String, String> prompt = new ConcurrentHashMap<>();

    @Async("chatResponseExecutor")
    @Override

    public CompletableFuture<String> generateChatResponse(Response res, String matchId) {

        return CompletableFuture.supplyAsync(() -> {

            prompt = promptGenerator.generatedChatPrompt(res);

            SystemMessage systemMessage = new SystemMessage(prompt.get("system"));

            log.debug("\nSystem message: [ {} ]", systemMessage);
            log.debug("\nUser message [ {} ]", prompt.get("user"));

            // 1. Build system prompt
            Conversation conversation = conversationRepo.getByMatchId(matchId)
                            .orElseGet(() -> new Conversation(matchId, new ArrayList<>()));

            log.debug("\nConversation: [ {} ]", conversation);

            // 2. Load full conversation
            List<ChatMessage> allMessages = new ArrayList<>(conversation.messages());

            allMessages.sort(Comparator.comparing(ChatMessage::sendDate));

            // 3. Split: recent + older
            int total = allMessages.size();
            int recentStart = Math.max(0, total - MAX_MESSAGES);
            List<ChatMessage> recent = allMessages.subList(recentStart, total);
            log.debug("\nRecent Messages: [ {} ]",recent);
            List<ChatMessage> older = allMessages.subList(0, recentStart);
            log.debug("\nOlder Messages: [ {} ]",older);

            // 4. Build message list for LLM
            List<Message> messages = new ArrayList<>();

            // Optional: Add summary of older messages
            if(!older.isEmpty()) {
                log.debug("\nOlder messages found, summarizing...");
                String olderText = summarizationService.formatMessagesForSummary(older, res.userId());
                log.debug("\nOlder Messages as test [ {} ]", olderText);
                String summary = summarizationService.summarizeOlderMessages(olderText);
                log.debug("\nOlder Messages Summary [ {} ]", summary);
                messages.add(new SystemMessage("Conversation summary so far: " + summary));
            }

            // Add recent messages in order
            for (ChatMessage chatMessage : recent) {
                log.debug("\nChat message found: [ {} ]", chatMessage);
                if (chatMessage.isUserMessage(res.userId())) {
                    messages.add(new UserMessage(chatMessage.content()));
                } else {
                    log.debug("Assistant message found for user id: [{}] ", res.userId());
                    messages.add(new AssistantMessage(chatMessage.content()));
                }
            }

            // Add current user message
            messages.add(new UserMessage(res.messagePrompt()));
            log.debug("\nCurrent message added: [ {} ]", res.messagePrompt());

            log.info("\nGenerating chat response for user [{}] on thread {}", res.name(), Thread.currentThread().getName());
            try {
                return chatClient
                        .prompt()
                        .system(prompt.get("system"))
                        .user(prompt.get("user"))
                        .messages(messages)
                        .call()
                        .content();

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate chat response", e);
            }
        }, chatResponseExecutor);
    }
}
