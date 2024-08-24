package com.example.tinder_ai_backend.user;

import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.encoder = new BCryptPasswordEncoder();
    }


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

    @GetMapping(path = "/user/all")
    ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @PostMapping(path = "/user/create")
    ResponseEntity<Optional<User>> createNewUser(@RequestBody NewUser newUser) {
        if (userRepo.existsAllByEmail(newUser.email())) {
            System.err.println("Email : " + newUser.email() + " already exist...");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exist");
        }

        String hashedPassword = encoder.encode(newUser.password());

        User user = new User(newUser.email(), hashedPassword);
        User createdUser = userRepo.save(user);
        return ResponseEntity.ok(userRepo.findById(createdUser.id()));
    }

    @DeleteMapping(path = "/user/delete")
    ResponseEntity<String> deleteAllUsers() {
        userRepo.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    @PostMapping("/user/login")
    public ResponseEntity<Optional<Profile>> userLogin(@RequestBody User req) {
        User userByEmail = userRepo.getUserByEmail(req.email());
        if (userByEmail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!encoder.matches(req.password(), userByEmail.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorised");
        }
        return ResponseEntity.ok(profileRepo.findById(userByEmail.profileId()));
    }
}
