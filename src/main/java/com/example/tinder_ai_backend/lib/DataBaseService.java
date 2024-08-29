package com.example.tinder_ai_backend.lib;

import com.example.tinder_ai_backend.conversations.ConversationRepo;
import com.example.tinder_ai_backend.matches.MatchRepo;
import com.example.tinder_ai_backend.profile.Profile;
import com.example.tinder_ai_backend.profile.ProfileRepo;
import com.example.tinder_ai_backend.session.UserSessionRepo;
import com.example.tinder_ai_backend.user.User;
import com.example.tinder_ai_backend.user.UserRepo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DataBaseService {

    private static final Logger logger = LogManager.getLogger(DataBaseService.class);
    private final static String PROFILES_JSON_FILE = "profiles.json";
    private final UserSessionRepo sessionRepo;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final MatchRepo matchRepo;
    private final ConversationRepo conversationRepo;
    private final Gson gson;
    PasswordEncoder passwordEncoder;
    public DataBaseService(UserSessionRepo sessionRepo, UserRepo userRepo, ProfileRepo profileRepo, MatchRepo matchRepo, ConversationRepo conversationRepo, Gson gson, PasswordEncoder passwordEncoder) {
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.matchRepo = matchRepo;
        this.conversationRepo = conversationRepo;
        this.gson = gson;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void purgeData() {
        logger.info("Going to purge data from database...");
        logger.info("Attempting to purge all Sessions...");
        sessionRepo.deleteAll();
        logger.info("All sessions deleted");

        logger.info("Attempting to purge all Users...");
        userRepo.deleteAll();
        logger.info("All Users deleted");

        logger.info("Attempting to purge all Profiles");
        profileRepo.deleteAll();
        logger.info("All profiles deleted");

        logger.info("Attempting to purge all Matches...");
        matchRepo.deleteAll();
        logger.info("All Matches deleted");

        logger.info("Attempting to purge all Conversations");
        conversationRepo.deleteAll();
        logger.info("All conversations deleted");

        logger.info("Database no purged and ready to be seeded...");
    }

    public void seedDataBase() {
        logger.info("Attempting to seed database...");
        logger.info("Attempting to read json files and create a list of profiles...");

        try {
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());
            logger.info("Profile list created from file, attempting to save Profiles to database...");
            profileRepo.saveAll(profileList);
            logger.info("Profiles saved...");

            logger.info("Attempting to create Users from Profiles...");
            List<User> userList = new ArrayList<>();
            profileList.forEach(profile -> userList.add(
                    new User(
                            profile.userId(),
                            profile.firstName().toLowerCase().concat(profile.lastName().toLowerCase()).concat("@gmail.com"),
                            passwordEncoder.encode("password"),
                            new Date(),
                            false,
                            null,
                            null
                    ))
            );
            logger.info("Users created from, attempting to save to database...");
            userRepo.saveAll(userList);
            logger.info("Users saved to database...");
        } catch (Exception e) {
            logger.error("Unable to read json file error: \n", e);
        }

        logger.info("Database seeded successfully...");
    }
}
