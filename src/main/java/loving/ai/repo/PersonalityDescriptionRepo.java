package loving.ai.repo;

import loving.ai.dto.personalities.PersonalityDescription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityDescriptionRepo extends MongoRepository<PersonalityDescription, String> {
}
