package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.matches.Match;
import com.example.tinder_ai_backend.matches.MatchRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final MatchRepo matchRepo;

    @GetMapping(path = "/conversation/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conversation>> getConversations() {
        return ResponseEntity.ok(Optional.of(conversationRepo.findAll()).orElseThrow(() -> {
            System.err.println("No conversations found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversations found");
        }));
    }

    @PostMapping(path = "/conversation/add/{conversationId}")
    public ResponseEntity<Optional<Conversation>> addMessage(@PathVariable("conversationId") String conversationId, @RequestBody ChatMessage message) {

        if (conversationId.isBlank() || conversationId.isEmpty() || message == null || message.messageText().isEmpty()) {
            System.err.println("No message text or conversation id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No conversationId and or message text");
        }

        Optional<Conversation> conversation = Optional.of(conversationRepo.findById(conversationId))
                .orElseThrow(() -> {
                    System.err.println("Unable to find conversation by id: [ " + conversationId + " ]");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find conversation by id");
                });

        return ResponseEntity.of(Optional.of(conversation.map((m) -> {
            m.messages().add(new ChatMessage(UUID.randomUUID().toString(), new Date(), message.messageText()));
            conversationRepo.save(m);
            return m;
        })));

    }

    @GetMapping(path = "/conversation/find/{conversationId}")
    public ResponseEntity<Optional<Conversation>> getConversationById(@PathVariable("conversationId") String conversationId) {

        return ResponseEntity.ok(Optional.of(conversationRepo.findById(conversationId).orElseThrow(() -> {
            System.err.println("No conversation found for id: [" + conversationId + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        })));
    }

    @PostMapping(path = "/conversation/from-to", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Optional<Conversation>> getConversationFromTo(@RequestBody Match req) {
        Match match = matchRepo.findByFromTo(req.fromProfileId(), req.toProfileId())
                .orElseThrow(() -> {
                    System.err.println("Unable to find conversation for match: \n" + req);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

        Conversation conversation = conversationRepo.findById(match.conversationId())
                .orElseThrow(() -> {
                    System.err.println("Unable to find conversation with id: [ " +  match.conversationId() + " ]");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });
        return ResponseEntity.ok(Optional.of(conversation));
    }

    @DeleteMapping(path = "/conversation/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delConversations() {
        try {
            conversationRepo.deleteAll();
            return ResponseEntity.ok("All messages deleted..");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to delete conversations" + e.getMessage());
        }
    }
}