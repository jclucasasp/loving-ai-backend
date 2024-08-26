package com.example.tinder_ai_backend.session;

import org.springframework.data.mongodb.repository.*;

import java.util.Optional;

public interface UserSessionRepo extends MongoRepository<UserSession, String> {

    @ExistsQuery("{'userId':?0}")
    boolean existsByUserId(String userId);

    @DeleteQuery("{'userId':?0}")
    Boolean deleteByUserId(String userId);

    @Query("{'userId':?0}")
    Optional<UserSession> getUserSessionByUserId(String userId);

//    @Update( value = "{'userId':?0}")
//    Optional<UserSession> updateSessionByUserId(String userid);
}
