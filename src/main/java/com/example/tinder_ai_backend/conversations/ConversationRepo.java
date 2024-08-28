package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.matches.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.Date;
import java.util.Optional;


public interface ConversationRepo extends MongoRepository<Conversation, String> {

    @Query("{'matchId':?0}")
    Optional<Conversation> getByMatchId(String matchId);
}


