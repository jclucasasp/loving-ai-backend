package com.example.tinder_ai_backend.matches;

import com.example.tinder_ai_backend.conversations.Conversation;
import com.example.tinder_ai_backend.conversations.ConversationRepo;
import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@AllArgsConstructor
public class MatchController {

    private static final Logger logger = LogManager.getLogger(MatchController.class);
    private final MatchRepo matchRepo;
    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @PostMapping(value = "/match/create")
    public ResponseEntity<Match> createMatch(@RequestBody Match req) {
        if (req == null || req.profileId().isBlank() || req.toProfileId().isBlank()) {
            logger.debug("No body params...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body unreadable");
        }

        if (matchRepo.existByProfileId(req.toProfileId())) {
            logger.debug("Match already exist for profile id: [ {} ]", req.profileId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Match already made");
        }

        Conversation conversation = new Conversation(UUID.randomUUID().toString(), new ArrayList<>());
        conversationRepo.save(conversation);

        Match match = new Match(UUID.randomUUID().toString(), new Date(), req.profileId(), req.toProfileId());
        matchRepo.save(match);

        return ResponseEntity.ok(match);
    }

    @GetMapping(value = "/matches/all")
    public ResponseEntity<List<Match>> findAll() {
        return ResponseEntity.ok(Optional.of(matchRepo.findAll()).orElseThrow(() -> {
            logger.debug("No matches found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @PostMapping(value = "/match/profiles")
    public ResponseEntity<List<Optional<Profile>>> getAllProfilesById(@RequestBody Match[] req) {
        List<Optional<Profile>> profilesById = new ArrayList<>();

        try {
            for (Match match : req) {
                profilesById.add(profileRepo.getProfileByUserId(match.toProfileId()));
            }
        } catch (Exception e) {
            logger.error("Unable to populate profiles...");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to populate profiles", e);
        }

        return ResponseEntity.ok(profilesById);
    }

    @DeleteMapping(value = "/match/delete-by-id")
    public ResponseEntity<String> delMatchById(@RequestBody Profile profile) {
        System.out.printf("Incoming delete request for id: " + profile.userId());
        try {
            matchRepo.deleteById(matchRepo.findByProfileId(profile.userId()).id());
        } catch (Exception e) {
            logger.error("Unable to delete profile id: [ {} ]", profile.userId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete profile id: " + profile.userId(), e);
        }
        return ResponseEntity.ok("Match id: " + profile.userId() + " deleted");
    }

    @DeleteMapping(value = "/matches/delete-all")
    public ResponseEntity<String> delAllMessages() {
        matchRepo.deleteAll();
        logger.debug("All matches deleted...");
        return ResponseEntity.ok("All matches deleted");
    }
}
