package ai.memory.ai.chat.memory.profile;

import org.springframework.data.mongodb.repository.*;

import java.util.Optional;

public interface ProfileRepo extends MongoRepository<Profile, String> {

    @ExistsQuery("{'userId':?0}")
    boolean existsProfileByUserId(String userId);

    @Query("{'firstName':?0}")
    Optional<Profile> getProfileByFirstName(String firstName);

    @Query("{'userId':?0}")
    Optional<Profile> getProfileByUserId(String userId);

    @Aggregation(pipeline = {
            "{$match:{'ai':true,'gender':?0}}}",
            "{$sample:{size:1}}"
    })
    Profile getRandomProfile(Gender gender);

    @Query("{'userId':?0}")
    @Update("{'$set':{'verified':?1}}")
    void findFirstAndUpdateVerified(String userId, boolean verified);
}

