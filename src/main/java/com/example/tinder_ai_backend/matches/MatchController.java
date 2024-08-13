package com.example.tinder_ai_backend.matches;

import com.example.tinder_ai_backend.conversations.Conversation;
import com.example.tinder_ai_backend.conversations.ConversationRepo;
import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
public class MatchController {

    private final MatchRepo matchRepo;
    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @GetMapping(value = "/matches/all")
    public ResponseEntity<List<Match>> findAll() {
        return ResponseEntity.ok(Optional.of(matchRepo.findAll()).orElseThrow(() -> {
            System.err.println("No matches found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @PostMapping(value = "/match/profiles")
    public ResponseEntity<List<Optional<Profile>>> getAllProfilesById(@RequestBody Match[] req) {
        List<Optional<Profile>> profilesById = new ArrayList<>();

        try {
            for (Match match : req) {
                profilesById.add(profileRepo.findById(match.toProfileId()));
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to populate profiles", e);
        }
        return ResponseEntity.ok(profilesById);
    }

    @PostMapping(value = "/match/create")
    public ResponseEntity<Match> createMatch(@RequestBody Match req) {
        if (req == null || req.fromProfileId().isEmpty() || req.toProfileId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body unreadable");
        }

        if (matchRepo.existByProfileId(req.toProfileId())) {
            System.err.println("Match already exist for profile id: [ " + req.fromProfileId() + " ]");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Match already made");
        }

        Conversation conversation = new Conversation(UUID.randomUUID().toString(), req.fromProfileId(), new ArrayList<>());
        conversationRepo.save(conversation);

        Match match = new Match(UUID.randomUUID().toString(), new Date(), req.fromProfileId(), req.toProfileId(), conversation.id());
        matchRepo.save(match);

        return ResponseEntity.ok(match);
    }

    @DeleteMapping(value = "/matches/del")
    public ResponseEntity<String> delAllMessages() {
        matchRepo.deleteAll();
        return ResponseEntity.ok("All matches deleted");
    }
}
