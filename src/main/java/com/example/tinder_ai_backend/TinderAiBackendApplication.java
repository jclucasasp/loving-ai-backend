package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.profile.Gender;
import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
@RequiredArgsConstructor
public class TinderAiBackendApplication implements CommandLineRunner {

    private final ProfileRepo profileRepo;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {
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

//            if (existingProfile.isPresent()) {
//                System.out.println("\nID [" + profile.id() + "] already exist, exiting...");
//                return;
//            }
            System.out.println("\nPreparing to write new profile to mongoDB...");
            profileRepo.save(profile);

        } catch (Exception e) {
            System.out.println("\nUnable to save profile: \n" + e.getCause());
        } finally {
            System.out.println("\nGetting current profiles in mongoDB");
            profileRepo.findAll().forEach(System.out::println);
        }
    }

}
