package com.example.ai_dating_backend.profile;

import org.springframework.data.mongodb.repository.*;

import java.util.Optional;

public interface ProfileRepo extends MongoRepository<Profile, String> {

    @ExistsQuery("{'userId':?0}")
    boolean existsProfileByUserId(String userId);

    @Query("{'firstName':?0}")
    Optional<Profile> getProfileByFirstName(String firstName);

    @Query("{'userId':?0}")
    Optional<Profile> getProfileByUserId(String userId);

    @Aggregation(pipeline = {"{$sample:{size:1}}"})
    Profile getRandomProfile();
}
