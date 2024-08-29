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

    @PostMapping(path = "/match/create")
    public ResponseEntity<Match> createMatch(@RequestBody Match req) {

        if (!profileRepo.existsProfileByUserId(req.profileId())) {
            logger.error("No user found for id: [ {} ]",req.profileId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (matchRepo.existByProfileId(req.toProfileId())) {
            logger.debug("Match already exist for profile id: [ {} ]", req.profileId());
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

    @PostMapping(path = "/matches/all")
    public ResponseEntity<List<Match>> findAll(@RequestBody Profile req) {
        List<Match> matchList = matchRepo.findAllProfileId(req.userId())
        .orElseThrow(() -> {
           logger.error("Unable to find matches for profile id: [ {} ]",req.userId());
           return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        return ResponseEntity.ok(matchList);
    }

    @PostMapping(path = "/match/profiles")
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

    @DeleteMapping(path = "/match/delete-by-id")
    public ResponseEntity<String> delMatchById(@RequestBody Profile profile) {
        Match matchFound = matchRepo.findByProfileId(profile.userId())
                .orElseThrow(() -> {
                    logger.error("Unable to find match for profile id: [ {} ]", profile.userId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        try {
            matchRepo.deleteById(matchFound.id());
        } catch (Exception e) {
            logger.error("Unable to delete profile id: [ {} ]", profile.userId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete profile id: " + profile.userId(), e);
        }
        return ResponseEntity.ok("Match id: " + profile.userId() + " deleted");
    }

    @DeleteMapping(path = "/matches/delete-all")
    public ResponseEntity<String> delAllMessages() {
        matchRepo.deleteAll();
        logger.debug("All matches deleted...");
        return ResponseEntity.ok("All matches deleted");
    }
}
