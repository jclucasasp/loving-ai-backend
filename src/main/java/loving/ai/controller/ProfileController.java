package loving.ai.controller;

import lombok.extern.log4j.Log4j2;
import loving.ai.dto.profile.Gender;
import loving.ai.dto.profile.Profile;
import loving.ai.repo.ProfileRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
public class ProfileController {

    private final ProfileRepo profileRepo;

    private ProfileController(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping(path = "/api/profile/all")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(Optional.of(profileRepo.findAll()).orElseThrow(() -> {
            log.error("No profiles found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No profiles found");
        }));
    }

    @PostMapping(path = "/api/profile/id")
    public ResponseEntity<Profile> getProfileById(@RequestBody Profile profile) {
        return ResponseEntity.ok(profileRepo.findById(profile.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping(path = "/api/profile/id")
    public ResponseEntity<String> deleteProfileById(@RequestBody Profile profile) {

        if (!profileRepo.existsById(profile.getUserId())) {
            log.error("No user exist for user id: [ {} ]", profile.getUserId());
        }
        profileRepo.deleteById(profile.getUserId());
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping(path = "/api/profile/name")
    public ResponseEntity<Profile> getProfileByName(@RequestBody() Profile req) {

        if (req == null || req.getFirstName().isBlank()) {
            log.error("Incoming request is null or Firstname is missing...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No params specified");
        }

        return ResponseEntity.ok(profileRepo.getProfileByFirstName(req.getFirstName())
                .orElseThrow(() -> {
                    log.error("Nothing found under firstname : [ {} ]", req.getFirstName());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

    @PostMapping(path = "/api/profile/random")
    public ResponseEntity<Profile> getRandomProfile(@RequestBody Profile req) {
        log.info("Requesting random profile for gender: [ {} ]", req.getGender());

        if (req.getGender().equals(Gender.MALE)) {
            req.setGender(Gender.FEMALE);
        } else {
            req.setGender(Gender.MALE);
        }
        return ResponseEntity.ok(Optional.of(profileRepo.getRandomProfile(req.getGender())).orElseThrow(() -> {
            log.error("No profiles to return...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    @PostMapping(path = "/api/profile/update")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile req) {

        System.out.println(req);

        if (req == null || req.getFirstName().isBlank()) {
            log.error("Empty body...");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Profile updatedProfile = profileRepo.save(req);
        if (updatedProfile.getUserId().isBlank()) {
            log.error("Unable to update profile for user: [{}]", req.getUserId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Profile updated for user: [{}]",req.getUserId());

        return ResponseEntity.ok(updatedProfile);
    }
}