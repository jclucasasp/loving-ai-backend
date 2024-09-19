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
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.*;

@Service
public class DataBaseService implements DataBaseServiceInterface {

    private static final Logger logger = LogManager.getLogger(DataBaseService.class);
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

    @Override
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

    @Override
    public void seedPersonalities() {
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
