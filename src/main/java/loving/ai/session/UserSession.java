package loving.ai.session;

import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.UUID;

public record UserSession(
        @MongoId(FieldType.STRING)
        String sessionId,
        String userId,
        Date logInDate,
        Date logOutDate) {

    public UserSession(String userId, Date logInDate, Date logOutDate) {
        this(UUID.randomUUID().toString(), userId, logInDate, logOutDate);
    }
}
