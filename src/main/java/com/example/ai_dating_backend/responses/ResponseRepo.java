package com.example.ai_dating_backend.responses;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepo extends MongoRepository<Response, String> {
}