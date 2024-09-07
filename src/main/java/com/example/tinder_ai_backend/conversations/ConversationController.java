package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.matches.Match;
import com.example.tinder_ai_backend.matches.MatchRepo;
import com.example.tinder_ai_backend.responses.Response;
import com.example.tinder_ai_backend.services.ResponseService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
public class ConversationController {

    private static final Logger logger = LogManager.getLogger(ConversationController.class);
    private final ConversationRepo conversationRepo;
    private final ResponseService responseService;
    private final MatchRepo matchRepo;

    @GetMapping(path = "/conversation/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conversation>> getConversations() {
        return ResponseEntity.ok(Optional.of(conversationRepo.findAll()).orElseThrow(() -> {
            logger.debug("No conversations found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversations found");
        }));
    }

    // This would have to change if not using AI
    @PostMapping(path = "/conversation/add/{matchId}")
    public ResponseEntity<Conversation> addMessage(@PathVariable("matchId") String matchId, @RequestBody Response req) throws ExecutionException, InterruptedException {

        // Finding the users current conversation
        Conversation fromConversation = conversationRepo.getByMatchId(matchId)
                .orElseThrow(() -> {
                    logger.debug("Unable to find conversation by id: [ {} ]", matchId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find conversation by id");
                });

        // Find the users current match
        Match matchFrom = matchRepo.findById(matchId).orElseThrow(() -> {
            logger.debug("Unable to find match for conversation id: [ {} ]", matchId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // Find the recipients match
        Match matchTo = matchRepo.findByFromTo(matchFrom.toProfileId(), matchFrom.profileId()).orElseThrow(() -> {
            logger.error("Unable to find match profile for profile id: [ {} }", matchFrom.toProfileId());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        System.out.println("Recipient match id: [ " + matchTo.id() + " ]");

        // Find the recipients conversation
        Conversation toConversation = conversationRepo.getByMatchId(matchTo.id()).orElseThrow(() -> {
            logger.error("Unable to find conversation recipient for match id: [ {} ]", matchTo.id());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        // generate a response from the Ai
        String conversationResponse = responseService.generateChatResponse(req).get();

        // saving the current conversation on the user side
        fromConversation.messages()
                .addAll(Arrays.asList(
                        new ChatMessage(UUID.randomUUID().toString(), matchFrom.profileId(), matchFrom.toProfileId(), new Date(), req.messagePrompt()),
                        new ChatMessage(UUID.randomUUID().toString(), matchFrom.toProfileId(), matchFrom.profileId(), new Date(), conversationResponse)
                ));
        conversationRepo.save(fromConversation);

        // saving the current conversation on the recipient side
        toConversation.messages()
                .addAll(Arrays.asList(
                        new ChatMessage(UUID.randomUUID().toString(), matchTo.toProfileId(), matchTo.profileId(), new Date(), req.messagePrompt()),
                        new ChatMessage(UUID.randomUUID().toString(), matchTo.profileId(), matchTo.toProfileId(), new Date(), conversationResponse)
                ));
        conversationRepo.save(toConversation);

        return ResponseEntity.ok(fromConversation);
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