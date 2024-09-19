package com.example.ai_dating_backend.matches;

import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends MongoRepository<Match, String> {

    @ExistsQuery("{'toProfileId':?0}")
    Boolean existByProfileId(String toProfileId);

    @Query("{'toProfileId':?0}")
    Optional<Match> findByProfileId(String toProfileId);

    @Query("{$and:[{'profileId':?0},{'toProfileId':?1}]}")
    Optional<Match> findByFromTo(String profileId, String toProfileId);

    @Query("{'profileId':?0}}")
    Optional<List<Match>> findAllProfileId(String profileId);
}
