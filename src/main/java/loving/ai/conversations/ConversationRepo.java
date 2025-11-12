package loving.ai.conversations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;


public interface ConversationRepo extends MongoRepository<Conversation, String> {

    @Query("{'matchId':?0}")
    Optional<Conversation> getByMatchId(String matchId);

//    Optional<Conversation> findTop20ByMatchIdOrderBySendDateDesc(String matchId);
}
