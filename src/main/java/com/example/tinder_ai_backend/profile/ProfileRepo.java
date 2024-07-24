package com.example.tinder_ai_backend.profile;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepo extends MongoRepository<Profile, String> {
}
