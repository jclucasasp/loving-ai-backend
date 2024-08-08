package com.example.tinder_ai_backend.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class ProfileCreateService {

    private final static String PROFILES_JSON_FILE = "profiles.json";
    private final ProfileRepo profileRepo;

    private ProfileCreateService(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    public void createProfiles() {
        Gson gson = new Gson();
        try {
            System.out.println("Getting a list of profiles from jason file...");
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());
            System.out.println("Attempting to write profiles to mongo database...");
            profileRepo.saveAll(profileList);
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong: \n" + e);
        } finally {
            System.out.println("New profiles uploaded and ready to go!");
        }
    }
}
