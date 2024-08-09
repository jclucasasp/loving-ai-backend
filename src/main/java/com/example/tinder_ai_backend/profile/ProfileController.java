package com.example.tinder_ai_backend.profile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class ProfileController {

    private final ProfileRepo profileRepo;

    private ProfileController(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping("/profiles")
    public ResponseEntity<Optional<List<Profile>>> getAllProfiles() {
        return ResponseEntity.ok(Optional.ofNullable(Optional.of(profileRepo.findAll()).orElseThrow(() -> {
            System.err.println("No profiles found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No profiles found");
        })));
    }

    @GetMapping("/profile")
    public ResponseEntity<Optional<Profile>> getProfileByName(@RequestBody() Profile req) {
        System.out.println(req.firstName());
        if (req.firstName() == null || req.firstName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No params specified");
        }
        return ResponseEntity.ok(Optional.ofNullable(profileRepo.getProfileByFirstName(req.firstName())
                .orElseThrow(() -> {
                    System.err.println("Nothing found under firstname : [ " + req.firstName() + " ]");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        ));
    }

    @GetMapping("/profile/random")
    public Profile getRandomProfile() {
        return profileRepo.getRandomProfile();
    }
}
