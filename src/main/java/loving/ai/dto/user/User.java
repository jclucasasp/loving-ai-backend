package loving.ai.dto.user;

import com.mongodb.lang.Nullable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Document(collection = "users")
public record User (
        @MongoId(FieldType.STRING)
        String id,
        @Indexed(name = "User-email-idx", unique = true)
        String email,
        String password,
        Date create_date,
        @Nullable
        Date end_date,
        @Nullable
        Date passwordResetDate,
        @Nullable
        Boolean active,
        @Nullable
        Set<String> roles,
        Boolean verified
) {


    public User(String email, String password) {
        this(UUID.randomUUID().toString(), email, password, new Date(), null, null, null, null, false);
    }
}