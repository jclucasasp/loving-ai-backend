package ai.memory.ai.chat.memory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RedisConfig implements RedisSerializer<UserMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    RedisTemplate<String, ConcurrentHashMap<String, List<Message>>> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ConcurrentHashMap<String, List<Message>>> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        objectMapper.registerModule(new JavaTimeModule());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }

    @Override
    public byte[] serialize(UserMessage value) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new RuntimeException("Error serialising UserMessage class: ", e);
        }
    }

    @Override
    public UserMessage deserialize(byte[] bytes) throws SerializationException {
        try {
            return objectMapper.readValue(bytes, UserMessage.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing UserMessage class: ", e);
        }
    }
}
