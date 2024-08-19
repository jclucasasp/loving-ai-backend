package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @GetMapping(path = "/conversation/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Conversation>> getConversations() {
        return ResponseEntity.ok(Optional.of(conversationRepo.findAll()).orElseThrow(() -> {
            System.err.println("No conversations found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No conversations found");
        }));
    }

    @PostMapping(path = "/conversation/add/{conversationId}")
    public ResponseEntity<Optional<Conversation>> addMessage(@PathVariable("conversationId") String conversationId, @RequestBody ChatMessage message) {

        if (conversationId.isBlank() || conversationId.isEmpty() || message.messageText().isEmpty() || message.messageText().isBlank()) {
            System.err.println("No message text or conversation id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No conversationId and or message text");
        }

        Optional<Conversation> conversation = Optional.ofNullable(conversationRepo.findById(conversationId).orElseThrow(() -> {
            System.err.println("Conversation not found for id: [ " + conversationId + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id: " + conversationId);
        }));

        return ResponseEntity.of(Optional.of(conversation.map((m) -> {
            m.messages().add(new ChatMessage(message.messageText(), message.toProfile(), LocalDateTime.now()));
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
    public ResponseEntity<Optional<Conversation>> getConversationFromTo(@RequestBody Conversation req) {
        return ResponseEntity.ok(conversationRepo.findByFromTo(req.fromProfileId(), req.toProfileId()));
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