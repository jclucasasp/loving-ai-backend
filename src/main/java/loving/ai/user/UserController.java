package loving.ai.user;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loving.ai.profile.Gender;
import loving.ai.profile.Profile;
import loving.ai.profile.ProfileRepo;
import loving.ai.services.EmailSender;
import loving.ai.services.FileCopier;
import loving.ai.services.OTPService;
import loving.ai.services.PasswordAndOTPGenerator;
import loving.ai.session.UserSession;
import loving.ai.session.UserSessionRepo;
import lombok.extern.log4j.Log4j2;
import loving.ai.utils.JwtUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

@Log4j2
@RestController
public class UserController {

    private final JwtUtil jwtUtil;
    private final PasswordAndOTPGenerator otpGenerator;
    private final UserSessionRepo sessionRepo;
    private final EmailSender emailSender;
    private final ProfileRepo profileRepo;
    private final FileCopier fileCopier;
    private final UserRepo userRepo;
    private final OTPService otpService;
    private final Environment environment;
    PasswordEncoder encoder;

    public UserController(UserRepo userRepo, ProfileRepo profileRepo, UserSessionRepo sessionRepo, EmailSender emailSender, PasswordAndOTPGenerator otpGenerator, FileCopier fileCopier, OTPService otpService, JwtUtil jwtUtil, Environment environment) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
        this.otpGenerator = otpGenerator;
        this.emailSender = emailSender;
        this.fileCopier = fileCopier;
        this.jwtUtil = jwtUtil;
        this.environment = environment;
        this.encoder = new BCryptPasswordEncoder();
        this.otpService = otpService;
    }

    @GetMapping(path = "/api/user/{email}", params = "email")
    ResponseEntity<User> getUserByEmail(@PathVariable(name = "email") String email) {
        return ResponseEntity.ok(userRepo.getUserByEmail(email).orElseThrow(() -> {
            log.error("No user found for email: [ {} ]", email);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for email: [ " + email + " ]");
        }));
    }

    @GetMapping(path = "/api/user/all")
    ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.getAll().orElseThrow(() -> {
            log.error("No users found...");
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }

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
            log.error("Email : {} already exist...", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exist");
        }

        String fileName = UUID.randomUUID().toString().concat(".jpg");
        boolean imageSaved = fileCopier.createFile(imageFile, fileName, gender.toString().toLowerCase());

        if (!imageSaved) {
            log.error("Unable to save image for user: [{}]", email);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String hashedPassword = encoder.encode(password);

        User user = new User(email, hashedPassword);

        User saveUser = userRepo.save(user);
        if (saveUser.email().isBlank()) {
            log.error("Unable to create user: [{}]", email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("New user created: [{}]", saveUser.email());

        Profile newProfile;
        if (gender.equals(Gender.FEMALE)) {
            newProfile = new Profile(saveUser.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "women/".concat(fileName), false, false, myersBriggsPersonalityType);

        } else {
            newProfile = new Profile(saveUser.id(), firstName, lastName, age,
                    ethnicity, gender, bio, "men/".concat(fileName), false, false, myersBriggsPersonalityType);
        }

        Profile savedProfile = profileRepo.save(newProfile);
        if (savedProfile.getUserId().isEmpty()) {
            log.error("Unable to create profile: [{}]", email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("New profile created: [{}]", email);
        return ResponseEntity.ok("User " + firstName + " " + lastName + " created");
    }

    @DeleteMapping(path = "/api/user/delete")
    ResponseEntity<String> deleteAllUsers() {
        userRepo.deleteAll();
        return ResponseEntity.ok("All users deleted");
    }

    @PostMapping(path = "/api/user/login")
    public ResponseEntity<Profile> userLogin(@RequestBody User req, HttpServletRequest request, HttpServletResponse response) {

        if (req.email() == null || req.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = userRepo.getUserByEmail(req.email()).orElseThrow();
        if (!encoder.matches(req.password(), userFound.password()) || userFound.end_date() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        log.debug("User logged in [{}]", req.email());

        Profile userFoundProfile = profileRepo.getProfileByUserId(userFound.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Set<String> roles = userFound.roles() != null ? userFound.roles() : Set.of("USER");
        String access = jwtUtil.accessToken(req.email(), roles);
        String refresh = jwtUtil.refreshToken(req.email());

        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        ResponseCookie accessCookie = ResponseCookie.from("access_token", access)
                .httpOnly(true)
                .secure(!isDev)
                .sameSite(isDev ? "Lax" : "None")
                .path("/")
                .maxAge(Duration.ofMinutes(15))           // short-lived
                .domain(isDev ? null : ".loving-ai.com")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(!isDev)
                .sameSite(isDev ? "Lax" : "None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .domain(isDev ? null : ".loving-ai.com")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(userFoundProfile);
    }

    @PostMapping(path = "/api/user/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Claims claims = jwtUtil.parse(refreshToken);
        String email = claims.getSubject();
        User user = userRepo.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!jwtUtil.valid(refreshToken, email))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Set<String> roles = user.roles() != null ? user.roles() : Set.of("USER");
        String newAccess = jwtUtil.accessToken(email, roles);

        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccess)
                .httpOnly(true)
                .secure(!isDev)
                .sameSite(isDev ? "Lax" : "None")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .domain(isDev ? null : ".loving-ai.com")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .build();
    }

    @PostMapping(path = "/api/user/logout")
    public ResponseEntity<HttpStatus> userLogout(@RequestBody User req) {

        if (req.id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = userRepo.findById(req.id()).orElseThrow(() -> {
            log.error("No user found for id [{}]", req.id());
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });

        userRepo.findFirstByIdAndUpdate(userFound.id(), false);

        UserSession sessionFound = sessionRepo.getUserSessionByUserId(userFound.id(), null).orElseThrow();

        sessionRepo.findFirstByIdAndUpdate(sessionFound.sessionId(), new Date());
        log.info("Logout request for user: [{}]", userFound.email());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(path = "/api/user/otp")
    public ResponseEntity<HttpStatus> Otp(@RequestBody User req) {

        log.info("\nIncoming OTP for [{}]", req);

        if (req.email() == null && req.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = null;
        String otp = null;

        if (req.id() != null) {
            userFound = findUser(req.id());
        }

        if (req.email() != null) {
            userFound = findUser(req.email());
        }

        if (userFound.end_date() != null) {
            log.error("Disabled user trying to log in: [{}]", userFound.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (otpService.getOtpHasMap(userFound.email()) == null) {
            log.info("\nNo email found for user: [{}]", otpService.getOtpHasMap(userFound.email()));
            otp = otpGenerator.generateOTP(6);
        } else {
            log.info("\nOTP already exist for user: [{}]", otpService.getOtpHasMap(userFound.email()));
            otp = otpService.getOtpHasMap(userFound.email());
        }

        log.debug("OTP [{}] generated for email [{}]", otp, userFound.email());

        otpService.otpTimer(userFound.email(), otp);

        final String message1 = "Please use this one time pin: " + otp + " to reset your password on Loving AI. \nPlease note that it will expire in 10 minutes!";
        final String message2 = "Welcome and thank you for choosing Loving AI. We hope that you enjoy the experience and we are always open to your feedback. \nPlease use this one time pin: " + otp + " to activate your profile on Loving AI. \nPlease note that it will expire in 10 minutes!";

        Profile userProfile = profileRepo.getProfileByUserId(userFound.id()).orElseThrow();

        HttpStatusCode httpStatusCode = emailSender.sendEmail(userFound.email(),
                "LovingAI: OTP", userProfile.getFirstName() + " " + userProfile.getLastName(), userProfile.isVerified() ? message1 : message2);

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
        log.info("User Verified: [{}]", userFound.email());

        emailSender.newSignUp(userFound.email());

        return ResponseEntity.ok(profileRepo.getProfileByUserId(userFound.id())
                .orElseThrow(() -> {
                    log.error("Unable to find profile for user: [{}]", userFound.id());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                })
        );
    }

//    @GetMapping(path = "/api/test")
//    public ResponseEntity<String> test() {
//        String token = jwtUtil.accessToken("test@lovingai.com", Set.of("USER"));
//        return ResponseEntity.ok("Generated JWT: " + token);
//    }

    @PostMapping(path = "/api/user/reset")
    public ResponseEntity<User> ResetPassword(@RequestBody NewUser req) {

        if (req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User userFound = findUser(req.email());

        if (!otpService.getOtpHasMap(req.email()).equals(req.otp())) {
            log.error("Incorrect OTP entered by user: [{}]", req.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (userFound.email().equalsIgnoreCase("jclucasasp@gmail.com")) {
            Objects.requireNonNull(userFound.roles()).add("ADMIN");
        } else {
            Objects.requireNonNull(userFound.roles()).add("USER");
        }

        User updatedUser = new User(
                userFound.id(),
                userFound.email(),
                encoder.encode(req.password()),
                userFound.create_date(),
                userFound.end_date(),
                new Date(),
                userFound.active(),
                userFound.roles()
        );

        User resetUser = userRepo.save(updatedUser);

        if (resetUser.email().isBlank()) {
            log.error("Unable to reset password for user: [{}]", req.email());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Password reset for user: [{}]", userFound.email());

        return ResponseEntity.ok(updatedUser);
    }

    private User findUser(String userId) {
        if (userId.contains("@")) {
            log.debug("Incoming request for user email: [{}]", userId);
            return userRepo.getUserByEmail(userId).orElseThrow(() -> {
                log.error("No user returned for email: [{}]", userId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND);
            });
        } else {
            log.debug("Incoming request for user Id: [{}]", userId);
            return userRepo.getUserById(userId).orElseThrow(() -> {
                log.error("No user returned for userId: [{}]", userId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND);
            });
        }
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
