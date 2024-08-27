package com.example.tinder_ai_backend.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class ProfileCreateService {

    private final static String PROFILES_JSON_FILE = "profiles.json";
    private static final Logger logger = LogManager.getLogger(ProfileCreateService.class);
    private final ProfileRepo profileRepo;

    private ProfileCreateService(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    //TODO: Generate users for each profile
    public void createProfiles() {
        Gson gson = new Gson();
        try {
            logger.info("Getting a list of profiles from jason file...");
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());

            logger.info("Attempting to write profiles to mongo database...");
            profileRepo.saveAll(profileList);
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong: \n" + e);
        } finally {
            logger.info("New profiles uploaded and ready to go!");
        }
    }

    public void deleteAllProfiles () {
        profileRepo.deleteAll();
        logger.info("All profiles deleted");
    }
}
