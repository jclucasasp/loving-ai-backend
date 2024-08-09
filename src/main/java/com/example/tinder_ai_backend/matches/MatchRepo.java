package com.example.tinder_ai_backend.matches;

import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepo extends MongoRepository<Match, String> {

    @ExistsQuery("{'fromProfileId': ?0}")
    Boolean existByProfileId(String profileId);
}
