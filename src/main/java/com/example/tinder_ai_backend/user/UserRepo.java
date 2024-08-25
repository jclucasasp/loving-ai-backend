package com.example.tinder_ai_backend.user;

import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepo extends MongoRepository<User, String> {
    @Query("{'email':?0}")
    User getUserByEmail(String email);
    Boolean existsAllByEmail(String email);

    @ExistsQuery("{'sessionId':?0}")
    Boolean sessionExistById(String sessionId);

    @Query("{'profileId':?0}")
    User getUserByProfileId(String profileId);
}




