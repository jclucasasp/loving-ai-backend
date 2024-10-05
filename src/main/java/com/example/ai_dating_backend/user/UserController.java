package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Profile;
import com.example.ai_dating_backend.profile.ProfileRepo;
import com.example.ai_dating_backend.services.EmailSender;
import com.example.ai_dating_backend.services.PasswordGenerator;
import com.example.ai_dating_backend.session.UserSession;
import com.example.ai_dating_backend.session.UserSessionRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@Log4j2
@RestController
public class UserController {

    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final UserSessionRepo sessionRepo;
    private final EmailSender emailSender;
    private final PasswordGenerator passwordGenerator;
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, UserSessionRepo sessionRepo, EmailSender emailSender, PasswordGenerator passwordGenerator) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
        this.passwordGenerator = passwordGenerator;
        this.emailSender = emailSender;
        this.encoder = new BCryptPasswordEncoder();
    }


    @GetMapping(path = "/user/{email}", params = "email")
    ResponseEntity<User> getUserByEmail(@PathVariable(name = "email") String email) {
        return ResponseEntity.ok(userRepo.getUserByEmail(email).orElseThrow(() -> {
            log.debug("No user found for email: [ {} ]", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for email: [ " + email + " ]");
        }));
    }

    @GetMapping(path = "/user/all")
    ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.getAll().orElseThrow(() -> {
            log.debug("No users found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    //TODO: Set the date offset.
    // https://reflectoring.io/spring-timezones/
    @PostMapping(path = "/user/create")
    ResponseEntity<HttpStatus> createNewUser(@RequestBody NewUser newUser) {

        if (newUser.email().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userRepo.existsAllByEmail(newUser.email())) {
            log.debug("Email : {} already exist...", newUser.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exist");
        }

        String hashedPassword = encoder.encode(newUser.password());

        User user = new User(newUser.email(), hashedPassword);
        userRepo.save(user);

        Profile newProfile = new Profile(user.id(), newUser.firstName(), newUser.lastName(), newUser.age(),
                newUser.ethnicity(), newUser.gender(), newUser.bio(), newUser.imageUrl(), newUser.personalityTypeId());

        profileRepo.save(newProfile);

        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/user/delete")
    ResponseEntity<String> deleteAllUsers() {
        userRepo.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    @PostMapping("/user/login")
    public ResponseEntity<Profile> userLogin(@RequestBody User req) {

        if (req.email() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = userRepo.getUserByEmail(req.email())
                .orElseThrow(() -> {
                    log.debug("No user found");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

        if (!encoder.matches(req.password(), userFound.password())) {
            log.debug("Unauthorised");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorised");
        }

        boolean currentSession = sessionRepo.existsByUserIdAndLogOutDateIsNull(userFound.id());

        if (!currentSession) {
            UserSession session = new UserSession(userFound.id(), new Date(), null);
            sessionRepo.save(session);
        }

        userRepo.findFirstByIdAndUpdate(userFound.id(), true);

        return ResponseEntity.ok(profileRepo.getProfileByUserId(userFound.id())
                .orElseThrow(() -> {
                    log.debug("Unable to find profile for user");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

    @PostMapping(path = "/user/logout")
    public ResponseEntity<HttpStatus> userLogout(@RequestBody User req) {

        if (req.id() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = userRepo.findById(req.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        userRepo.findFirstByIdAndUpdate(userFound.id(), false);

        UserSession sessionFound = sessionRepo.getUserSessionByUserId(userFound.id(), null).orElseThrow();

        sessionRepo.findFirstByIdAndUpdate(sessionFound.sessionId(), new Date());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

   //TODO: Need to generate a one time pin to send via email for password reset.
   // Save this with a unique id and timer so that each new request doesn't interfere with the previous
    @PostMapping(path = "/user/otp")
    public ResponseEntity<HttpStatus> PasswordReset(@RequestBody User req) {

        if (req.email() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = userRepo.getUserByEmail(req.email()).orElseThrow(() -> {
            log.error("No user found for email: [{}]", req.email());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        String newPassword = encoder.encode(passwordGenerator.generatePassword(8));
        User updatedUser = new User(userFound.id(), userFound.email(), newPassword, userFound.create_date(), null, new Date());

        userRepo.save(updatedUser);

        final String message = "Please use this one time pin to reset your password. It will expire in 10 minutes";

        emailSender.sendEmail("there", message);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
