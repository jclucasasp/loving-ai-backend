package loving.ai.personalities;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonalityTypesRepo extends MongoRepository<PersonalitiesTypes, String> {
}
