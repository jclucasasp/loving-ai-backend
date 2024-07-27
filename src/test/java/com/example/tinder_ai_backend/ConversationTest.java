package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.conversations.*;
import com.example.tinder_ai_backend.conversations.ConversationController.*;
import com.example.tinder_ai_backend.profile.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@SpringBootTest
public class ConversationTest {

    @Test
    public void test_create_conversation_with_invalid_profileId() {
        ProfileRepo profileRepo = Mockito.mock(ProfileRepo.class);
        ConversationRepo conversationRepo = Mockito.mock(ConversationRepo.class);
        ConversationController controller = new ConversationController(conversationRepo, profileRepo);

        String invalidProfileId = "invalid-profile-id";
        Mockito.when(profileRepo.findById(invalidProfileId)).thenReturn(Optional.empty());

        ConversationRequest request = new ConversationRequest(invalidProfileId);

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            controller.createConversation(request);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), exception.getBody().getStatus());
        Mockito.verify(conversationRepo, Mockito.any()).save(Mockito.any());
    }

    @Test
    public void test_create_conversation_with_valid_profileId() {
        ProfileRepo profileRepo = Mockito.mock(ProfileRepo.class);
        ConversationRepo conversationRepo = Mockito.mock(ConversationRepo.class);
        ConversationController controller = new ConversationController(conversationRepo, profileRepo);

        String validProfileId = "2";
//        Mockito.when(profileRepo.findById(validProfileId)).thenReturn(Optional.of(new Profile()));

        ConversationRequest request = new ConversationRequest(validProfileId);
        Conversation conversation = controller.createConversation(request);

        Assertions.assertNotNull(conversation);
        Assertions.assertEquals(validProfileId, conversation.profileId());
        Mockito.verify(conversationRepo, Mockito.times(1)).save(conversation);
    }

}


