package com.example.ai_dating_backend.profile;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Log4j2
@CrossOrigin(origins = "*")
@RestController
public class ProfileController {

    private final ProfileRepo profileRepo;

    private ProfileController(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping("/profile/all")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(Optional.of(profileRepo.findAll()).orElseThrow(() -> {
            log.error("No profiles found...");
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
            log.debug("No user exist for user id: [ {} ]", profile.userId());
        }
        profileRepo.deleteById(profile.userId());
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/profile/name")
    public ResponseEntity<Profile> getProfileByName(@RequestBody() Profile req) {

        if (req == null || req.firstName().isBlank()) {
            log.debug("Firstname is missing from the body...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No params specified");
        }

        return ResponseEntity.ok(profileRepo.getProfileByFirstName(req.firstName())
                .orElseThrow(() -> {
                    log.debug("Nothing found under firstname : [ {} ]", req.firstName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

    @PostMapping(path = "/profile/random")
    public ResponseEntity<Profile> getRandomProfile(@RequestBody Profile req) {
        return ResponseEntity.ok(Optional.of(profileRepo.getRandomProfile(req.gender())).orElseThrow(() -> {
            log.debug("No profiles to return...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @PostMapping("/profile/update")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile req) {

        System.out.println(req);

        if (req == null || req.firstName().isBlank()) {
            log.debug("Empty body...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Profile updatedProfile = profileRepo.save(req);

        return ResponseEntity.ok(updatedProfile);
    }
}