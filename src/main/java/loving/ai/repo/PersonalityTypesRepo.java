package loving.ai.repo;

import loving.ai.dto.personalities.PersonalitiesTypes;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityTypesRepo extends MongoRepository<PersonalitiesTypes, String> {
}
