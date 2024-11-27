package ai.memory.ai.chat.memory.profile;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@Getter
@Setter
public class Profile {
        @Indexed(name = "ProfileId-idx")
        @MongoId(FieldType.STRING)
        String userId;
        String firstName;
        String lastName;
        int age;
        String ethnicity;
        Gender gender;
        String bio;
        String imageUrl;
        boolean ai;
        boolean verified;
        String myersBriggsPersonalityType;
}
