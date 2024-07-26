package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.conversations.ChatMessage;
import com.example.tinder_ai_backend.conversations.Conversation;
import com.example.tinder_ai_backend.conversations.ConversationRepo;
import com.example.tinder_ai_backend.profile.Gender;
import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@SpringBootApplication
@RequiredArgsConstructor
public class TinderAiBackendApplication implements CommandLineRunner {

    private final ProfileRepo profileRepo;

    private final ConversationRepo conversationRepo;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {

        try {
            Properties envProps = new Properties();
            envProps.load(new FileInputStream("src/main/resources/.env"));
            final String API_KEY = envProps.getProperty("API_KEY");
//            System.out.println("ENV Api Key: [ "+ API_KEY+ " ]");

            Properties appProps = new Properties();
            appProps.load(new FileInputStream("src/main/resources/application.properties"));
            appProps.setProperty("spring.ai.openai.api-key", API_KEY);
//            System.out.println("Application api key after being set from env: [ "+ appProps.getProperty("spring.ai.openai.api-key")+" ]");

        } catch (IOException io) {
            throw new RuntimeException("Unable to fetch .env: \n" + io.getCause());
        }

        Profile profile = new Profile(
                "1",
                "JC",
                "Lucas",
                47,
                "Caucasian",
                Gender.MALE,
                "Software Engineer",
                "foo.jpg",
                "SIGMA"
        );

        try {
            Optional<Profile> existingProfile = profileRepo.findById(profile.id());
            if (existingProfile.isPresent()) {
                System.out.println("\nID [" + profile.id() + "] already exist, updating...");
            } else {
                System.out.println("\nPreparing to create new profile...");
            }

            profileRepo.save(profile);

        } catch (Exception e) {
            System.out.println("\nUnable to save profile: \n" + e.getCause());
        } finally {
            System.out.println("\nGetting current profiles in mongoDB");
            profileRepo.findAll().forEach(System.out::println);
        }

        Conversation conversation = new Conversation("1", profile.id(), List.of(
                new ChatMessage("Ello Govner", profile.id(), LocalDateTime.now())

        ));

        try {
            System.out.println("Saving conversation...");
            conversationRepo.save(conversation);
            List<Conversation> conversations = conversationRepo.findAll();

            // The long loop below can be replaced with the shorter one...
            // conversations.forEach(System.out::println);

            for (Conversation c: conversations) {
                System.out.println("Conversation id: " + c.id());
                System.out.println("Profile id: " + c.profileId());

                for (ChatMessage chatMessage : c.messages()) {
                    System.out.println(chatMessage.messageText());
                }
            }
        } catch (Exception c) {
            System.out.println("Unable to save the conversation: \n" + c.getCause());
        } finally {
            System.out.println("Conversation with id: [ "+conversation.id()+" ] saved to mongodb...");
        }
    }
}