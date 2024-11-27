package ai.memory.ai.chat.memory.personalities;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityDescriptionRepo extends MongoRepository<PersonalityDescription, String> {
}
