package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.matches.Match;
import com.example.tinder_ai_backend.matches.MatchRepo;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
public class ConversationController {

    private static final Logger logger = LogManager.getLogger(ConversationController.class);
    private final ConversationRepo conversationRepo;
    private final MatchRepo matchRepo;

    @GetMapping(path = "/conversation/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conversation>> getConversations() {
        return ResponseEntity.ok(Optional.of(conversationRepo.findAll()).orElseThrow(() -> {
            logger.debug("No conversations found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversations found");
        }));
    }

    @PostMapping(path = "/conversation/add/{matchId}")
    public ResponseEntity<Conversation> addMessage(@PathVariable("matchId") String matchId, @RequestBody ChatMessage message) {

        Conversation conversation = conversationRepo.getByMatchId(matchId)
                .orElseThrow(() -> {
                    logger.debug("Unable to find conversation by id: [ {} ]", matchId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find conversation by id");
                });

        Match match = matchRepo.findById(matchId).orElseThrow(() -> {
            logger.debug("Unable to find match for conversation id: [ {} ]", matchId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        conversation.messages()
                .add(new ChatMessage(UUID.randomUUID().toString(), match.profileId(), match.toProfileId(), new Date(), message.messageText())
                );
        conversationRepo.save(conversation);

        logger.debug("Conversation saved with the new message...");
        return ResponseEntity.ok(conversation);
    }

    @GetMapping(path = "/conversation/find/{matchId}")
    public ResponseEntity<Optional<Conversation>> getConversationById(@PathVariable("matchId") String matchId) {

        return ResponseEntity.ok(Optional.of(conversationRepo.getByMatchId(matchId).orElseThrow(() -> {
            logger.debug("No conversation found for id: [ {} ]", matchId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        })));
    }

    @PostMapping(path = "/conversation/from-to")
    public ResponseEntity<Optional<Conversation>> getConversationFromTo(@RequestBody Match req) {
        logger.debug("Incoming match: [ {} ]", req);
        Match match = matchRepo.findByFromTo(req.profileId(), req.toProfileId())
                .orElseThrow(() -> {
                    logger.debug("Unable to find conversation for match: \n{}", req);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        logger.debug("Match found: [ {} ]", match);

        Conversation conversation = conversationRepo.getByMatchId(match.id())
                .orElseThrow(() -> {
                    logger.debug("Unable to find conversation with id: [ {} ]", match.id());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        logger.debug("Conversation found: [ {} ]", conversation);
        return ResponseEntity.ok(Optional.of(conversation));
    }

    @DeleteMapping(path = "/conversation/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delConversations() {
        try {
            conversationRepo.deleteAll();
            logger.debug("All messages deleted...");
            return ResponseEntity.ok("All messages deleted..");
        } catch (Exception e) {
            logger.error("Unable to delete conversations...");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to delete conversations" + e.getMessage());
        }
    }
}