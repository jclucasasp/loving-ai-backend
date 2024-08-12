package com.example.tinder_ai_backend.matches;

import com.example.tinder_ai_backend.profile.Profile;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MatchRepo extends MongoRepository<Match, String> {

    @ExistsQuery("{'fromProfileId': ?0}")
    Boolean existByProfileId(String profileId);

    @Query("{'ids': {$all:[...ids]}}")
    Collection<Profile> getProfilesById(List<String> ids);
}
