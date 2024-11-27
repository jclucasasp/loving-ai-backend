package ai.memory.ai.chat.memory.session;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.Date;
import java.util.Optional;

public interface UserSessionRepo extends MongoRepository<UserSession, String> {

    @Query("{$and:[{'userId':?0},{'logOutDate':?1}]}")
    Optional<UserSession> getUserSessionByUserId(String userId, Date logOutDate);

    @Query("{'sessionId':?0}")
    @Update("{'$set':{'logOutDate':?1}}")
    void findFirstByIdAndUpdate(String sessionId, Date logOutDate);

    boolean existsByUserIdAndLogOutDateIsNull(String userId);
}
