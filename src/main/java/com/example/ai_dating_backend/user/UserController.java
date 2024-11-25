package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Gender;
import com.example.ai_dating_backend.profile.Profile;
import com.example.ai_dating_backend.profile.ProfileRepo;
import com.example.ai_dating_backend.services.EmailSender;
import com.example.ai_dating_backend.services.FileCopier;
import com.example.ai_dating_backend.services.OTPService;
import com.example.ai_dating_backend.services.PasswordAndOTPGenerator;
import com.example.ai_dating_backend.session.UserSession;
import com.example.ai_dating_backend.session.UserSessionRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Log4j2
@RestController
public class UserController {

    private final PasswordAndOTPGenerator otpGenerator;
    private final UserSessionRepo sessionRepo;
    private final EmailSender emailSender;
    private final ProfileRepo profileRepo;
    private final FileCopier fileCopier;
    private final UserRepo userRepo;
    private final OTPService otpService;
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, UserSessionRepo sessionRepo, EmailSender emailSender, PasswordAndOTPGenerator otpGenerator, FileCopier fileCopier, OTPService otpService) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
        this.otpGenerator = otpGenerator;
        this.emailSender = emailSender;
        this.fileCopier = fileCopier;
        this.encoder = new BCryptPasswordEncoder();
        this.otpService = new OTPService();
    }

    @GetMapping(path = "/api/user/{email}", params = "email")
    ResponseEntity<User> getUserByEmail(@PathVariable(name = "email") String email) {
        return ResponseEntity.ok(userRepo.getUserByEmail(email).orElseThrow(() -> {
            log.debug("No user found for email: [ {} ]", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for email: [ " + email + " ]");
        }));
    }

    @GetMapping(path = "/api/user/all")
    ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.getAll().orElseThrow(() -> {
            log.debug("No users found...");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

    //TODO: Set the date offset.
    // https://reflectoring.io/spring-timezones/
    @PostMapping(path = "/api/user/create")
    ResponseEntity<String> createNewUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "otp", required = false) String otp,
            @RequestParam("age") int age,
            @RequestParam("ethnicity") String ethnicity,
            @RequestParam("gender") Gender gender,
            @RequestParam("bio") String bio,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam("myersBriggsPersonalityType") String myersBriggsPersonalityType
    ) {

        if (email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userRepo.existsAllByEmail(email)) {
            log.debug("Email : {} already exist...", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exist");
        }

        String fileName = UUID.randomUUID().toString().concat(".jpg");
        boolean imageSaved = fileCopier.createFile(imageFile, fileName, gender.toString().toLowerCase());

        if (!imageSaved) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String hashedPassword = encoder.encode(password);

        User user = new User(email, hashedPassword);
        userRepo.save(user);

        Profile newProfile;

        if (gender.equals(Gender.FEMALE)) {
            newProfile = new Profile(user.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "women/".concat(fileName), false, false, myersBriggsPersonalityType);

        } else {
            newProfile = new Profile(user.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "men/".concat(fileName), false, false, myersBriggsPersonalityType);
        }

        profileRepo.save(newProfile);

        return ResponseEntity.ok("User " + firstName + " " + lastName + " created");
    }

    @DeleteMapping(path = "/api/user/delete")
    ResponseEntity<String> deleteAllUsers() {
        userRepo.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    @PostMapping("/api/user/login")
    public ResponseEntity<Profile> userLogin(@RequestBody User req) {

        if (req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = findUser(req.email());

        if (!encoder.matches(req.password(), userFound.password()) || userFound.end_date() != null) {
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

    @PostMapping(path = "/api/user/logout")
    public ResponseEntity<HttpStatus> userLogout(@RequestBody User req) {

        if (req.id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = userRepo.findById(req.id()).orElseThrow(() -> {
            log.error("No user found for id [{}]", req.id());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        userRepo.findFirstByIdAndUpdate(userFound.id(), false);

        UserSession sessionFound = sessionRepo.getUserSessionByUserId(userFound.id(), null).orElseThrow();

        sessionRepo.findFirstByIdAndUpdate(sessionFound.sessionId(), new Date());
        log.info("User logged out");

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(path = "/api/user/otp")
    public ResponseEntity<HttpStatus> Otp(@RequestBody User req) {

        log.info("\nIncoming OTP for [{}]", req);

        if (req.email() == null && req.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = null;

        if (req.id() != null ) {
            userFound = findUser(req.id());
        }

        if (req.email() != null) {
            userFound = findUser(req.email());
        }

        if (userFound.end_date() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String message;
        String otp = otpGenerator.generateOTP(6);
        log.info("OTP [{}] generated for email [{}]", otp, userFound.email());

        otpService.otpTimer(userFound.email(), otp);

        final String message1 = "Please use this one time pin: " + otp + " to reset your password on Loving AI. \nPlease note that it will expire in 10 minutes!";
        final String message2 = "Welcome and thank you for choosing Loving AI. We hope that you enjoy the experience and we are always open to your feedback. \nPlease use this one time pin: " + otp + " to activate your profile on Loving AI. \nPlease note that it will expire in 10 minutes!";

        Profile userProfile = profileRepo.getProfileByUserId(userFound.id()).orElseThrow();

        HttpStatusCode httpStatusCode = emailSender.sendEmail(userFound.email(),
                "LovingAI: OTP", userProfile.getFirstName() + " " + userProfile.getLastName(), Boolean.TRUE.equals(userProfile.isVerified()) ? message1 : message2);

        return ResponseEntity.status(httpStatusCode).build();
    }

    @PostMapping(path = "/api/user/verify")
    public ResponseEntity<Profile> userActivate(@RequestBody NewUser req) {

        if (req.userId() == null || Objects.requireNonNull(req.otp()).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = findUser(req.userId());

        if (userFound == null || !req.otp().equals(otpService.getOtpHasMap(userFound.email()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        profileRepo.findFirstAndUpdateVerified(userFound.id(), true);

        return ResponseEntity.ok(profileRepo.getProfileByUserId(userFound.id()).orElseThrow());
    }

    @PostMapping(path = "/api/user/reset")
    public ResponseEntity<User> ResetPassword(@RequestBody NewUser req) {

        if (req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = findUser(req.email());

        if (!otpService.getOtpHasMap(req.email()).equals(req.otp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User updatedUser = new User(
                userFound.id(),
                userFound.email(),
                encoder.encode(req.password()),
                userFound.create_date(),
                userFound.end_date(),
                new Date(),
                null
        );

        userRepo.save(updatedUser);

        return ResponseEntity.ok(updatedUser);
    }

    private User findUser(String userId) {
        if (userId.contains("@")) {
            log.info("Incoming OTP request for user email: [{}]", userId);
           return userRepo.getUserByEmail(userId).orElseThrow();
        } else {
            log.info("Incoming OTP request for user Id: [{}]", userId);
            return userRepo.getUserById(userId).orElseThrow();
        }
    }
}
