package com.example.tinder_ai_backend.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepo extends MongoRepository<User, String> {
    @Query("{'email':?0}")
    User getUserByEmail(String email);
}
