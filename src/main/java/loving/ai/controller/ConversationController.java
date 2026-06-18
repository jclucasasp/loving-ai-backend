package loving.ai.controller;

import loving.ai.conversations.ChatMessage;
import loving.ai.conversations.Conversation;
import loving.ai.repo.ConversationRepo;
import loving.ai.dto.matches.Match;
import loving.ai.repo.MatchRepo;
import loving.ai.dto.responses.Response;
import loving.ai.services.interfaces.ResponseServiceInterface;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Log4j2
@AllArgsConstructor
@RestController
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ResponseServiceInterface responseService;
    private final MatchRepo matchRepo;

    @GetMapping(path = "/api/conversation/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conversation>> getConversations() {
        return ResponseEntity.ok(Optional.of(conversationRepo.findAll()).orElseThrow(() -> {
            log.error("No conversations found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversations found");
        }));
    }

    @PostMapping(path = "/api/conversation/add/{matchId}")
    public ResponseEntity<Conversation> addMessage(@PathVariable("matchId") String matchId, @RequestBody Response res) throws ExecutionException, InterruptedException {

        // Finding the users current conversation
        Conversation fromConversation = conversationRepo.getByMatchId(matchId)
                .orElseThrow(() -> {
                    log.error("Unable to find conversation by id: [ {} ]", matchId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find conversation by id");
                });

        log.debug("Found conversation: [ {} ]", fromConversation);

        // Find the users current match
        Match matchFrom = matchRepo.findById(matchId).orElseThrow(() -> {
            log.error("Unable to find match for conversation id: [ {} ]", matchId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        log.debug("Found match: [ {} ]", matchFrom);

        // Find the recipients match
        Match matchTo = matchRepo.findByFromTo(matchFrom.toProfileId(), matchFrom.profileId()).orElseThrow(() -> {
            log.error("Unable to find match profile for profile id: [ {} }", matchFrom.toProfileId());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        log.debug("Found match: [{}]", matchTo);
        log.debug("Recipient match id: [{}]", matchTo.id());

        // generate a response from the Ai
        String conversationResponse = responseService.generateChatResponse(res, matchId).get();

        log.debug("Generated response from AI: [{}]", conversationResponse);

        // saving the current conversation on the user side
        fromConversation.messages()
                .addAll(Arrays.asList(
                        new ChatMessage(UUID.randomUUID().toString(), matchFrom.profileId(), matchFrom.toProfileId(), new Date(), res.messagePrompt()),
                        new ChatMessage(UUID.randomUUID().toString(), matchFrom.toProfileId(), matchFrom.profileId(), new Date(), conversationResponse)
                ));

        Conversation savedConversation = conversationRepo.save(fromConversation);
        if ( savedConversation.messages().isEmpty()) {
            log.error("No messages found in conversation for matchId: [{}]", matchId);
        }

        return ResponseEntity.ok(fromConversation);
    }

    @GetMapping(path = "/api/conversation/find/{matchId}")
    public ResponseEntity<Optional<Conversation>> getConversationById(@PathVariable("matchId") String matchId) {

        return ResponseEntity.ok(Optional.of(conversationRepo.getByMatchId(matchId).orElseThrow(() -> {
            log.debug("No conversation found for id: [ {} ]", matchId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        })));
    }

    @PostMapping(path = "/api/conversation/from-to")
    public ResponseEntity<Optional<Conversation>> getConversationFromTo(@RequestBody Match req) {
        log.debug("Incoming match: [ {} ]", req);
        Match match = matchRepo.findByFromTo(req.profileId(), req.toProfileId())
                .orElseThrow(() -> {
                    log.error("Unable to find conversation for match: \n{}", req);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        log.debug("Match found: [ {} ]", match);

        Conversation conversation = conversationRepo.getByMatchId(match.id())
                .orElseThrow(() -> {
                    log.error("Unable to find conversation with id: [ {} ]", match.id());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        log.debug("Conversation found: [ {} ]", conversation);
        return ResponseEntity.ok(Optional.of(conversation));
    }

    @DeleteMapping(path = "/api/conversation/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delConversations() {
        try {
            conversationRepo.deleteAll();
            log.debug("All messages deleted...");
            return ResponseEntity.ok("All messages deleted..");
        } catch (Exception e) {
            log.error("Unable to delete conversations...");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to delete conversations" + e.getMessage());
        }
    }
}