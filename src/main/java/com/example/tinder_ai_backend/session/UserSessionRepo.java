package com.example.tinder_ai_backend.session;

import org.springframework.data.mongodb.repository.*;

import java.util.Date;
import java.util.Optional;

public interface UserSessionRepo extends MongoRepository<UserSession, String> {

    @Query("{$and:[{'userId':?0},{'logOutDate':?1}]}")
    Optional<UserSession> getUserSessionByUserId(String userId, Date logOutDate);

    @Query("{'sessionId':?0}")
    @Update("{'$set':{'logOutDate':?1}}")
    void findFirstByIdAndUpdate(String sessionId, Date logOutDate);
}
