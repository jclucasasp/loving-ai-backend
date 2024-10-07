package com.example.ai_dating_backend.services;

import com.example.ai_dating_backend.conversations.ConversationRepo;
import com.example.ai_dating_backend.matches.MatchRepo;
import com.example.ai_dating_backend.personalities.*;
import com.example.ai_dating_backend.profile.Profile;
import com.example.ai_dating_backend.profile.ProfileRepo;
import com.example.ai_dating_backend.services.interfaces.DataBaseServiceInterface;
import com.example.ai_dating_backend.session.UserSessionRepo;
import com.example.ai_dating_backend.user.User;
import com.example.ai_dating_backend.user.UserRepo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

// TODO: Replace the personality types in the json file to use the id of the personality types as well as in the ResponseService class
@Log4j2
@Service
public class DataBaseService implements DataBaseServiceInterface {

    private final static String PROFILES_JSON_FILE = "profiles.json";
    private final UserSessionRepo sessionRepo;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final MatchRepo matchRepo;
    private final ConversationRepo conversationRepo;
    private final PersonalityTypesRepo typesRepo;
    private final PersonalityDescriptionRepo descriptionRepo;
    private final Gson gson;
    PasswordEncoder passwordEncoder;

    public DataBaseService(UserSessionRepo sessionRepo,
                           UserRepo userRepo, ProfileRepo profileRepo,
                           MatchRepo matchRepo, ConversationRepo conversationRepo,
                           Gson gson, PasswordEncoder passwordEncoder, PersonalityTypesRepo typesRepo, PersonalityDescriptionRepo descriptionRepo) {
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.matchRepo = matchRepo;
        this.conversationRepo = conversationRepo;
        this.gson = gson;
        this.typesRepo = typesRepo;
        this.descriptionRepo = descriptionRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void purgeData() {
        log.info("Going to purge data from database...");
        log.info("Attempting to purge all Sessions...");
        sessionRepo.deleteAll();
        log.info("All sessions deleted");

        log.info("Attempting to purge all Users...");
        userRepo.deleteAll();
        log.info("All Users deleted");

        log.info("Attempting to purge all Profiles");
        profileRepo.deleteAll();
        log.info("All profiles deleted");

        log.info("Attempting to purge all Matches...");
        matchRepo.deleteAll();
        log.info("All Matches deleted");

        log.info("Attempting to purge all Conversations");
        conversationRepo.deleteAll();
        log.info("All conversations deleted");

        log.info("Database no purged and ready to be seeded...");
    }

    @Override
    public void updateAndSaveProfiles() {
        try {
            log.info("Attempting to read json files and create a list of profiles...");
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());
            log.info("Profile list created from file, attempting to update Profiles...");

            List<Profile> updatedList = profileList.stream().map(p -> {
                if (!p.ai()) {
                    log.info("No AI field, adding to new list");
                    return new Profile(UUID.randomUUID().toString(),
                            p.firstName(),
                            p.lastName(),
                            p.age(),
                            p.ethnicity(),
                            p.gender(),
                            p.bio(),
                            p.imageUrl(),
                            true,
                            p.myersBriggsPersonalityType());
                } else {
                    log.info("AI Field found, returning profile...");
                    return p;
                }
            }).toList();

            writeUpdatedJsonList(updatedList);

        } catch (IOException io) {
            log.error("Unable to process file: ", io);
        }
    }

    private void writeUpdatedJsonList(List<Profile> updatedProfiles) {
        log.info("Attempting to convert updated profiles to a json file...");
        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<List<Profile>>() {
        }.getType();

        File file = new File(PROFILES_JSON_FILE + ".updated.json");

        try (FileWriter writer = new FileWriter(file);) {
            log.info("Attempting to write updated profiles back to a json file...");
            gsonWriter.toJson(updatedProfiles, type, writer);
            log.info("New json file called profiles.updated.json");
        } catch (IOException e) {
            log.error("Unable to process the updated profile: ", e);
        }
    }

    @Override
    public void seedDataBase() {
        log.info("Attempting to seed database...");
        log.info("Attempting to read updated json files and create a list of profiles...");

        try {
            List<Profile> profileList = gson.fromJson(new FileReader(PROFILES_JSON_FILE), new TypeToken<List<Profile>>() {
            }.getType());
            log.info("Profile list created from file, attempting to save Profiles to database...");

            profileRepo.saveAll(profileList);
            log.info("Profiles saved...");

            log.info("Attempting to create Users from Profiles...");
            List<User> userList = new ArrayList<>();
            profileList.forEach(profile -> userList.add(
                    new User(
                            profile.userId(),
                            profile.firstName().toLowerCase().concat(profile.lastName().toLowerCase()).concat("@nomail.com"),
                            passwordEncoder.encode("password"),
                            new Date(),
                            null,
                            null
                    ))
            );
            log.info("Users created from, attempting to save to database...");
            userRepo.saveAll(userList);
            log.info("Users saved to database...");
        } catch (Exception e) {
            log.error("Unable to read json file error: \n", e);
        }

        log.info("Database seeded successfully...");
    }

    @Override
    public void seedPersonalities() {
        log.info("Going to purge personalities types from db...");
        typesRepo.deleteAll();
        descriptionRepo.deleteAll();

        log.info("Attempting to seed personalities...");
        Arrays.stream(TypesEnum.values()).forEach(p -> {

            PersonalitiesTypes type = typesRepo.insert(new PersonalitiesTypes(UUID.randomUUID().toString(), p));

            switch (p) {
                case ENFJ -> InsertDescription(type, "Warm and empathetic, ENFJs have a strong desire to help others.");
                case ENTJ ->
                        InsertDescription(type, "Confident and strategic thinkers, ENTJs value leadership and achievement.");
                case ENFP ->
                        InsertDescription(type, "Charismatic and imaginative, ENFPs are driven by their passions and interests.");
                case ESFP ->
                        InsertDescription(type, "Vibrant and enthusiastic, ESFPs live in the moment and love to entertain.");
                case ENTP ->
                        InsertDescription(type, "Innovative and versatile, ENTPs thrive on intellectual exploration and debate.");
                case ESFJ ->
                        InsertDescription(type, "Charismatic and supportive, ESFJs prioritize harmony and cooperation.");
                case ESTJ ->
                        InsertDescription(type, "Confident and assertive, ESTJs value stability, order, and leadership.");
                case ESTP ->
                        InsertDescription(type, "Action-oriented and adventurous, ESTPs thrive on excitement and challenge.");
                case INFJ ->
                        InsertDescription(type, "Charismatic and empathetic, INFJs have a strong desire to help others");
                case INFP ->
                        InsertDescription(type, "Imaginative and idealistic, INFPs are driven by their passions and values.");
                case INTJ ->
                        InsertDescription(type, "Independent and strategic thinkers, INTJs value innovation and progress.");
                case INTP ->
                        InsertDescription(type, "Logical and analytical, INTPs appreciate complex systems and theories.");
                case ISFJ -> InsertDescription(type, "Warm and supportive, ISFJs prioritize harmony and cooperation.");
                case ISFP -> InsertDescription(type, "Artistic and gentle souls, ISFPs value creativity and harmony.");
                case ISTJ ->
                        InsertDescription(type, "Practical and detail-oriented, ISTJs value stability and security.");
                case ISTP -> InsertDescription(type, "Versatile and flexible, ISTPs enjoy exploring new experiences.");
                default -> System.out.println("No matches");
            }
        });
    }

    private void InsertDescription(PersonalitiesTypes type, String desc) {
        descriptionRepo.insert(new PersonalityDescription(type.id(), desc));
    }

}
