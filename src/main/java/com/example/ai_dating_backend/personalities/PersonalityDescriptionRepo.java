package com.example.ai_dating_backend.personalities;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityDescriptionRepo extends MongoRepository<PersonalityDescription, String> {
}
