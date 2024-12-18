package ai.memory.ai.chat.memory.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Service
public class CustomChatMemoryStore implements ChatMemory {
    private String matchId;

    Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    private RedisTemplate<String, ConcurrentHashMap<String, List<Message>>> redisTemplate;

    public CustomChatMemoryStore(String matchId, RedisTemplate<String, ConcurrentHashMap<String, List<Message>>> redisTemplate) {
        this.matchId = matchId;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages.isEmpty()) {
            redisTemplate.opsForHash().putIfAbsent(matchId, matchId.concat(conversationId), new ArrayList<>());
        } else {
            List<String> messageText = messages.stream().map(Message::toString).toList();
            redisTemplate.opsForHash().put(matchId, matchId.concat(conversationId), messageText);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<String> history = (List<String>) redisTemplate.opsForHash().get(matchId, matchId.concat(conversationId));
        if (history == null) {
            return List.of();
        }
        List<Message> all = history
                .stream().
                map(text -> (Message) new UserMessage(text))
                .toList();

        return all.stream().skip(Math.max(0, all.size() - lastN)).toList();
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.opsForHash().delete(matchId, conversationId);
    }
}
