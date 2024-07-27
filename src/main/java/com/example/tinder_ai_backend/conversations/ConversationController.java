package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @PostMapping(value = "/conversation")
    public Conversation createConversation(@RequestBody ConversationRequest request) {

//        profileRepo.findById(request.authorId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean profileId = profileRepo.findById(request.profileId()).isPresent();

        if (!profileId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id: " + request.profileId);
        }

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        conversationRepo.save(conversation);
        return conversation;
    }

    @PostMapping(value = "/conversation/{conversationId}")
    public Optional<Conversation> addMessage(@PathVariable("conversationId") String conversationId, @RequestBody ChatMessage message) {

        try {
            if (message.messageText().isBlank()|| conversationId.isBlank()) {
                return Optional.empty();
            }
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cant be blank " + e.getMessage());
        }

        Optional<Conversation> conversation = conversationRepo.findById(conversationId);
        conversation.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id: " + conversationId));
        conversationRepo.findById(message.authorId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No author id found for id: " + message.authorId()));
        //TODO: Validate that the author of the message does indeed belong to the correct profile

        return conversation.map((m) -> {
            m.messages().add(new ChatMessage(message.messageText(), conversationId, LocalDateTime.now()));
            conversationRepo.save(m);
            return m;
        });
    }

    public record ConversationRequest(String profileId) {
    }
}
