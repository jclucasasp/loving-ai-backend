package com.example.tinder_ai_backend.responses;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepo extends MongoRepository<Response, String> {
}