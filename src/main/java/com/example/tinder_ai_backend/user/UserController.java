package com.example.tinder_ai_backend.user;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
public class UserController {

    private final UserRepo userRepo;

    @GetMapping(path = "/user/{email}", params = "email")
    ResponseEntity<User> getUserByEmail(@PathVariable(name = "email") String email) {
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please include a valid email address");
        }
        return ResponseEntity.ok(Optional.of(userRepo.getUserByEmail(email)).orElseThrow(() -> {
            System.err.println("No user found for email: [ " + email + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for email: [ " + email + " ]");
        }));
    }

    @PostMapping(path = "/user/create")
    ResponseEntity<User> createNewUser(@RequestBody User newUser) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newUser.password());

        User user = new User(UUID.randomUUID().toString(), newUser.profileId(), newUser.email(), hashedPassword, new Date(), newUser.end_date(), null, newUser.matchId());
//        User createdUser = userRepo.save(user);
        return ResponseEntity.ok(user);
    }
}
