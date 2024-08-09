package com.example.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;


public interface ConversationRepo extends MongoRepository<Conversation, String> {

    @Query("{fromProfileId:?0}")
    Optional<Conversation> findByProfileId(String profileId);
}


