package com.example.ai_dating_backend.personalities;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityTypesRepo extends MongoRepository<PersonalitiesTypes, String> {
}
