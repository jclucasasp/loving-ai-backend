package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;


public interface ConversationRepo extends MongoRepository<Conversation, String> {

    @Query("{$and:[{'fromProfileId':?0},{'toProfileId':?1}]}")
    Optional<Conversation> findByFromTo(String fromProfileId, String toProfileId);
}


