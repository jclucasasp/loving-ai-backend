package com.example.tinder_ai_backend.profile;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProfileRepo extends MongoRepository<Profile, String> {

    @Query("{'firstName': ?0}")
    Optional<Profile> getProfileByFirstName(String firstName);

    @Aggregation(pipeline = {"{$sample:{size:1}}"})
    Profile getRandomProfile();
}
