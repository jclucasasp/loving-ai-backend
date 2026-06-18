package loving.ai.repo;

import loving.ai.dto.matches.Match;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends MongoRepository<Match, String> {

    @ExistsQuery("{$and:[{'profileId':?0},{'toProfileId':?1}]}")
    Boolean existByProfileId(String profileId, String toProfileId);

    @Query("{'toProfileId':?0}")
    Optional<Match> findByProfileId(String toProfileId);

    @Query("{$and:[{'profileId':?0},{'toProfileId':?1}]}")
    Optional<Match> findByFromTo(String profileId, String toProfileId);

    @Query("{'profileId':?0}}")
    Optional<List<Match>> findAllProfileId(String profileId);
}
