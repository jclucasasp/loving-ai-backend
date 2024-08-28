package com.example.tinder_ai_backend.matches;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface MatchRepo extends MongoRepository<Match, String> {

    @ExistsQuery("{'profileId': ?0}")
    Boolean existByProfileId(String profileId);

    @Query("{'toProfileId':?0}")
    Optional<Match> findByProfileId(String toProfileId);

    @Query("{$and:[{'profileId':?0},{'toProfileId':?1}]}")
    Optional<Match> findByFromTo(String profileId, String toProfileId);
}
