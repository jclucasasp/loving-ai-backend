package com.example.tinder_ai_backend.profile;

import com.example.tinder_ai_backend.user.User;
import com.example.tinder_ai_backend.user.UserRepo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileCreateService {

    private final static String PROFILES_JSON_FILE = "profiles.json";
    private static final Logger logger = LogManager.getLogger(ProfileCreateService.class);
    private final ProfileRepo profileRepo;
    private final UserRepo userRepo;

    private ProfileCreateService(ProfileRepo profileRepo, UserRepo userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    public void createProfiles() {
        Gson gson = new Gson();
        try {
            logger.info("Getting a list of profiles from jason file...");
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());


            logger.info("Creating users from current profiles...");
            List<User> userList = new ArrayList<>();
            profileList.forEach((profile -> userList.add(new User(profile.firstName().concat(profile.lastName().concat("@gmail.com")), "password"))));

            if (userList.isEmpty()) {
                logger.error("Unable to populate a list of users from profiles list...");
                return;
            }
            logger.info("Attempting to write users to mongo database...");
            userRepo.saveAll(userList);

            logger.info("Attempting to write profiles to mongo database...");
            profileRepo.saveAll(profileList);

        } catch (Exception e) {
            throw new RuntimeException("Something went wrong: \n" + e);
        } finally {
            logger.info("New profiles and users uploaded and ready to go!");
        }
    }

    public void deleteAllProfiles () {
        profileRepo.deleteAll();
        logger.info("All profiles deleted");
    }
}
