package com.example.ai_dating_backend.user;

import com.example.ai_dating_backend.profile.Gender;
import com.example.ai_dating_backend.profile.Profile;
import com.example.ai_dating_backend.profile.ProfileRepo;
import com.example.ai_dating_backend.services.EmailSender;
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

import java.io.*;
import java.nio.file.Path;
import java.util.*;

@CrossOrigin(origins = "*")
@Log4j2
@RestController
public class UserController {

    private final PasswordAndOTPGenerator otpGenerator;
    private final UserSessionRepo sessionRepo;
    private final EmailSender emailSender;
    private final ProfileRepo profileRepo;
    private final UserRepo userRepo;
    private final Map<String, String> userOtpHashMap = new HashMap<>();
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, UserSessionRepo sessionRepo, EmailSender emailSender, PasswordAndOTPGenerator otpGenerator) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
        this.otpGenerator = otpGenerator;
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
    //TODO: Create a welcome email for and otp for verification
    @PostMapping(path = "/user/create")
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

        String imageName = UUID.randomUUID().toString().concat(".jpeg");
        Path path = Path.of("src/main/resources/static/images/"+gender.toString().toLowerCase()+"/"+imageName);

        if (!imageFile.isEmpty()) {
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
                stream.write(imageFile.getBytes());
                log.info("Success writing image.jpeg");
            } catch (IOException e) {
                log.error("Unable to safe file with exception: ", e);
                return ResponseEntity.internalServerError().body("Unable to save image");
            }
        }

        String hashedPassword = encoder.encode(password);

        User user = new User(email, hashedPassword);
        userRepo.save(user);

        Profile newProfile;

        if (gender.equals(Gender.FEMALE)) {
            newProfile = new Profile(user.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "women/".concat(imageName), false, myersBriggsPersonalityType);

        } else {
            newProfile = new Profile(user.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "men/".concat(imageName), false, myersBriggsPersonalityType);
        }

        profileRepo.save(newProfile);

        return ResponseEntity.ok("User " + firstName + " " + lastName + " created");
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

        User userFound = findUserByEmail(req.email());

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

    @PostMapping(path = "/user/logout")
    public ResponseEntity<HttpStatus> userLogout(@RequestBody User req) {

        if (req.id() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = userRepo.findById(req.id()).orElseThrow(() -> {
            log.error("No user found for id [{}]", req.id());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        userRepo.findFirstByIdAndUpdate(userFound.id(), false);

        UserSession sessionFound = sessionRepo.getUserSessionByUserId(userFound.id(), null).orElseThrow();

        sessionRepo.findFirstByIdAndUpdate(sessionFound.sessionId(), new Date());
        log.info("User logged out");

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(path = "/user/otp")
    public ResponseEntity<HttpStatus> Otp(@RequestBody User req) {

        log.info("Incoming OTP request for [{}]", req.email());
        if (req.email() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = findUserByEmail(req.email());

        if (userFound.end_date() != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String otp = otpGenerator.generateOTP(6);
        log.info("OTP [{}] generated for email [{}]", otp, req.email());

        userOtpHashMap.put(userFound.email(), otp);

        otpTimer(req.email());

        final String message = "Please use this one time pin: [" + otp + "]  to reset your password on LovingAI. \nPlease note that it will expire in 10 minutes";

        Profile userProfile = profileRepo.getProfileByUserId(userFound.id()).orElseThrow();

        HttpStatusCode httpStatusCode = emailSender.sendEmail(userFound.email(),
                "LovingAI: OTP", userProfile.getFirstName() + " " + userProfile.getLastName(), message);

        return ResponseEntity.status(httpStatusCode).build();
    }

    @PostMapping(path = "/user/reset")
    public ResponseEntity<User> ResetPassword(@RequestBody NewUser req) {

        if (req.email().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User userFound = findUserByEmail(req.email());

        if (!userOtpHashMap.get(req.email()).equals(req.otp())) {
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

    private User findUserByEmail(String email) {
        return userRepo.getUserByEmail(email).orElseThrow(() -> {
            log.error("No user found for email: [{}]", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });
    }

    private void otpTimer(String userEmail) {

        log.info("OTP Timer started for user: [{}]", userEmail);
        Timer timer = new Timer(userEmail);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("OTP expired for user: [{}]", userEmail);
                userOtpHashMap.remove(userEmail);
            }
        };
        // expires after 10 minutes
        timer.schedule(task, 600000);
    }
}
