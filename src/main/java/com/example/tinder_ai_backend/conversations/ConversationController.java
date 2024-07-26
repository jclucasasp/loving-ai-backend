package com.example.tinder_ai_backend.conversations;

import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;

    @PostMapping("/conversation")
    public Conversation createConversation(@RequestBody ConversationRequest request) {

//        profileRepo.findById(request.profileId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean profileId = profileRepo.findById(request.profileId()).isPresent();

        if (!profileId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        conversationRepo.save(conversation);
        return conversation;
    }

    public record ConversationRequest(String profileId){}
}
