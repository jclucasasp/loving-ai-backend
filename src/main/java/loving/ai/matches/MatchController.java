package loving.ai.matches;

import loving.ai.conversations.Conversation;
import loving.ai.conversations.ConversationRepo;
import loving.ai.profile.Profile;
import loving.ai.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Log4j2
@RestController
@AllArgsConstructor
public class MatchController {

    private final MatchRepo matchRepo;
    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @PostMapping(path = "/api/match/create")
    public ResponseEntity<Match> createMatch(@RequestBody Match req) {

        if (!profileRepo.existsProfileByUserId(req.profileId())) {
            log.error("No user found for id: [ {} ]", req.profileId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (matchRepo.existByProfileId(req.profileId(), req.toProfileId())) {
            log.debug("Match already exist for profile id: [ {} ]", req.profileId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Match already made");
        }

        /* This is only for AI to automatch.
         * If real world application you would have to create toMatch logic
         * */
        Match matchFrom = new Match(UUID.randomUUID().toString(), new Date(), req.profileId(), req.toProfileId());
        matchRepo.save(matchFrom);
        Match matchTo = new Match(UUID.randomUUID().toString(), new Date(), req.toProfileId(), req.profileId());
        matchRepo.save(matchTo);

        Conversation conversationFrom = new Conversation(matchFrom.id(), new ArrayList<>());
        conversationRepo.save(conversationFrom);
        Conversation conversationTo = new Conversation(matchTo.id(), new ArrayList<>());
        conversationRepo.save(conversationTo);

        return ResponseEntity.ok(matchFrom);
    }

    @PostMapping(path = "/api/matches/all")
    public ResponseEntity<List<Match>> findAll(@RequestBody Profile req) {
        List<Match> matchList = matchRepo.findAllProfileId(req.getUserId()).orElseThrow(() -> {
            log.error("Unable to find matches for profile id: [ {} ]", req.getUserId());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        return ResponseEntity.ok(matchList);
    }

    @PostMapping(path = "/api/match/profiles")
    public ResponseEntity<List<Optional<Profile>>> getAllProfilesById(@RequestBody Match[] req) {
        List<Optional<Profile>> profilesById = new ArrayList<>();

        try {
            for (Match match : req) {
                profilesById.add(profileRepo.getProfileByUserId(match.toProfileId()));
            }
        } catch (Exception e) {
            log.error("Unable to populate profiles...");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to populate profiles", e);
        }

        return ResponseEntity.ok(profilesById);
    }

    @DeleteMapping(path = "/api/match/delete-by-id")
    public ResponseEntity<String> delMatchById(@RequestBody Profile profile) {
        Match matchFound = matchRepo.findByProfileId(profile.getUserId()).orElseThrow(() -> {
            log.error("Unable to find match for profile id: [{}]", profile.getUserId());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        try {
            matchRepo.deleteById(matchFound.id());
        } catch (Exception e) {
            log.error("Unable to delete profile id: [ {} ]", profile.getUserId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete profile id: " + profile.getUserId(), e);
        }

        log.info("UserId: [{}] deleted matched profileId: [{}]",profile.getUserId(), matchFound.profileId());

        return ResponseEntity.ok("Match id: " + profile.getUserId() + " deleted");
    }

    @DeleteMapping(path = "/api/matches/delete-all")
    public ResponseEntity<String> delAllMessages() {
        matchRepo.deleteAll();
        log.debug("All matches deleted...");
        return ResponseEntity.ok("All matches deleted");
    }
}
