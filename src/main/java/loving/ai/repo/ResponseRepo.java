package loving.ai.repo;

import loving.ai.dto.responses.Response;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepo extends MongoRepository<Response, String> {
}