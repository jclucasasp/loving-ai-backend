package ai.memory.ai.chat.memory.responses;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepo extends MongoRepository<Response, String> {
}