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

@RestController
@AllArgsConstructor
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @GetMapping(value = "/conversation/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Conversation> getConversations() {
        return conversationRepo.findAll();
    }

    @PostMapping(value = "/conversation")
    public Conversation createConversation(@RequestBody ConversationRequest request) {

        Conversation conversation;

        if (request.profileId == null || request.profileId.isBlank()) {
            System.err.println("Profile id is blank!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "profileId is blank or missing");
        }

        profileRepo.findById(request.profileId()).orElseThrow(() -> {
            System.err.println("No profile found for id: [ " + request.profileId + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        Optional<Conversation> existingConv = Optional.ofNullable(conversationRepo.findByProfileId(request.profileId())).orElseThrow(() -> {
            System.err.println("Unable to retrieve an existing conversation for profile id: [ "+request.profileId+" ]");
            return new ResponseStatusException(HttpStatus.BAD_REQUEST);
        });

        if (existingConv.isPresent()) {
            System.out.println("Conversation id already created, exiting...");
            throw  new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            System.out.println("No id for conversation found, creating new conversation...");
            conversation = new Conversation(
                    UUID.randomUUID().toString(),
                    request.profileId(),
                    new ArrayList<>()
            );
            conversationRepo.save(conversation);
        }
        return conversation;
    }

    @PostMapping(value = "/conversation/{conversationId}")
    public ResponseEntity<Optional<Conversation>> addMessage(@PathVariable("conversationId") String conversationId, @RequestBody ChatMessage message) {

        if (message.profileId() == null || message.profileId().isBlank()) {
            System.err.println("Profile id is blank!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId is blank or missing");
        }

        Optional<Conversation> conversation = Optional.ofNullable(conversationRepo.findById(conversationId).orElseThrow(() -> {
            System.err.println("Conversation not found for id: [ " + conversationId + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id: " + conversationId);
        }));

        return ResponseEntity.of(Optional.of(conversation.map((m) -> {
            m.messages().add(new ChatMessage(message.messageText(), message.profileId(), LocalDateTime.now()));
            conversationRepo.save(m);
            return m;
        })));
    }

    @GetMapping(value = "/conversation/{conversationId}")
    public Optional<Conversation> getConversationById(@PathVariable("conversationId") String conversationId) {

        return Optional.ofNullable(conversationRepo.findById(conversationId).orElseThrow(() -> {
            System.err.println("No conversation found for id: [" + conversationId + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @DeleteMapping(value = "/conversation/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> delConversations() {
        try {
            conversationRepo.deleteAll();
            return ResponseEntity.ok("All messages deleted..");
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete conversations" + e.getMessage());
        }
    }

    public record ConversationRequest(String profileId) {
    }
}