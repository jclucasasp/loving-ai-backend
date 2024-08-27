package com.example.tinder_ai_backend.user;

import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {
    @Query(value = "{'email':?0}", fields = "{'id':1, 'email':1, 'password':1 'create_date':1, 'active':1, 'end_date':1, 'passwordResetDate':1}")
    Optional<User> getUserByEmail(String email);

    @Query(value = "{'id':?*}", fields = "{'id':1, 'email':1, 'create_date':1, 'active':1, 'end_date':1, 'passwordResetDate':1}")
    Optional<List<User>> getAll();

    @ExistsQuery("{'email':?0}")
    Boolean existsAllByEmail(String email);

    @Query("{'id':?0}")
    @Update("{'$set':{'active':?1}}")
    void findFirstByIdAndUpdate(String id, boolean active);
}




