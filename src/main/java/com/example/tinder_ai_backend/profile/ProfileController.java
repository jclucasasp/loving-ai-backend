package com.example.tinder_ai_backend.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class ProfileController {

    private static final Logger logger = LogManager.getLogger(ProfileController.class);
    private final ProfileRepo profileRepo;

    private ProfileController(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping("/profile/all")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(Optional.of(profileRepo.findAll()).orElseThrow(() -> {
            logger.error("No profiles found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No profiles found");
        }));
    }

    @PostMapping(value = "/profile/id")
    public ResponseEntity<Profile> getProfileById(@RequestBody Profile profile) {
        return ResponseEntity.ok(profileRepo.findById(profile.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping(path = "/profile/id")
    public ResponseEntity<String> deleteProfileById(@RequestBody Profile profile) {

        if (!profileRepo.existsById(profile.userId())) {
            logger.debug("No user exist for user id: [ {} ]", profile.userId());
        }
        profileRepo.deleteById(profile.userId());
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/profile/name")
    public ResponseEntity<Profile> getProfileByName(@RequestBody() Profile req) {
        if (req == null || req.firstName().isBlank()) {
            logger.debug("Firstname is missing from the body...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No params specified");
        }
        return ResponseEntity.ok(profileRepo.getProfileByFirstName(req.firstName())
                .orElseThrow(() -> {
                    logger.debug("Nothing found under firstname : [ {} ]", req.firstName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

    @GetMapping("/profile/random")
    public ResponseEntity<Profile> getRandomProfile() {
        return ResponseEntity.ok(Optional.of(profileRepo.getRandomProfile()).orElseThrow(() -> {
            logger.debug("No profiles to return...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @GetMapping("/profile/index/stats")
    public void getProfileStats() {

    }
}