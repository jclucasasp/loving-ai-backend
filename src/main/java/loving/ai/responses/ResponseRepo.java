package loving.ai.responses;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepo extends MongoRepository<Response, String> {
}