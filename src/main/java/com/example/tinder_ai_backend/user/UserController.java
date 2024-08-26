package com.example.tinder_ai_backend.user;

import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import com.example.tinder_ai_backend.session.UserSession;
import com.example.tinder_ai_backend.session.UserSessionRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
public class UserController {

    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final UserSessionRepo sessionRepo;
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, PasswordEncoder encoder, UserSessionRepo sessionRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
        this.encoder = new BCryptPasswordEncoder();
    }


    @GetMapping(path = "/user/{email}", params = "email")
    ResponseEntity<User> getUserByEmail(@PathVariable(name = "email") String email) {
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please include a valid email address");
        }
        return ResponseEntity.ok(userRepo.getUserByEmail(email).orElseThrow(() -> {
            System.err.println("No user found for email: [ " + email + " ]");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for email: [ " + email + " ]");
        }));
    }

    @GetMapping(path = "/user/all")
    ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.getAll().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
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

        Profile newProfile = new Profile(user.id(), newUser.firstName(), newUser.lastName(), newUser.age(),
                newUser.ethnicity(), newUser.gender(), newUser.bio(), newUser.imageUrl(), newUser.myersBriggsPersonalityType());

        profileRepo.save(newProfile);

        return ResponseEntity.ok(userRepo.findById(createdUser.id()));
    }

    @DeleteMapping(path = "/user/delete")
    ResponseEntity<String> deleteAllUsers() {
        userRepo.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    @PostMapping("/user/login")
    public ResponseEntity<Profile> userLogin(@RequestBody User req) {
        User userFound = userRepo.getUserByEmail(req.email())
                .orElseThrow(() -> {
                    System.err.println("No user found");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

        if (userFound == null) {
            System.err.println("User not found for email: [ " + req.email() + " ] ");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Optional<UserSession> userSessionFound = sessionRepo.getUserSessionByUserId(userFound.id());

        if (userSessionFound.isPresent() && userSessionFound.get().sessionId() != null) {
            System.err.println("User already logged in");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already active");
        }

        if (!encoder.matches(req.password(), userFound.password())) {
            System.err.println("Unauthorised");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorised");
        }

        UserSession userSession = new UserSession(UUID.randomUUID().toString(), userFound.id(), new Date());
        sessionRepo.save(userSession);

        return ResponseEntity.ok(profileRepo.getProfileByUserId(userFound.id())
                .orElseThrow(() -> {
                    System.err.println("Unable to find profile for user");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

    // TODO: implement a session id  and userSessionId to be able to update the current session
    @PostMapping(path = "/user/logout")
    public ResponseEntity<String> userLogout(@RequestBody UserSession req) {
        if (req == null) {
            System.err.println("User body empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User body empty");
        }

        UserSession foundUserSession = sessionRepo.getUserSessionByUserId(req.userId()).orElseThrow(() -> {
            System.err.println("No user session");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        return ResponseEntity.ok("User logged out successfully");
    }
}
