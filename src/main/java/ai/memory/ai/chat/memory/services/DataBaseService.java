package ai.memory.ai.chat.memory.services;

import ai.memory.ai.chat.memory.conversations.ConversationRepo;
import ai.memory.ai.chat.memory.matches.MatchRepo;
import ai.memory.ai.chat.memory.personalities.*;
import ai.memory.ai.chat.memory.profile.Profile;
import ai.memory.ai.chat.memory.profile.ProfileRepo;
import ai.memory.ai.chat.memory.session.UserSessionRepo;
import ai.memory.ai.chat.memory.user.User;
import ai.memory.ai.chat.memory.user.UserRepo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

@Log4j2
@Service
public class DataBaseService {

    // Make sure the correct file in the .properties before running...
    @Value("${spring.json.profile.folder.name}")
    private String PROFILES_FOLDER;

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

    public void purgeData() {
        log.info("Going to purge data from database...");

        long count = profileRepo.count();

        if (count != 0) {
            log.info("Database is already seeded...");
            return;
        }

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

        seedDataBase();

        long typesCount = typesRepo.count();
        long descriptionCount = descriptionRepo.count();

        if (typesCount != 0 && descriptionCount != 0) {
            log.info("Personalities already seeded...");
            return;
        }

        log.info("Going to purge personalities types from db...");
        typesRepo.deleteAll();
        descriptionRepo.deleteAll();

        seedPersonalities();

        log.info("Database seeded...");
    }

    public void updateAndSaveProfiles() {

        File[] profiles = GetProfiles();
        if (profiles == null) {
            log.error("No files in folder [{}]...", PROFILES_FOLDER);
            return;
        }

        try {
            log.info("Attempting to read json files and create a list of profiles...");

            for (File profile : profiles) {

                if (profile.getName().endsWith(".txt")) {
                    continue;
                }
                List<Profile> profileList = gson.fromJson(new FileReader(profile), new TypeToken<List<Profile>>() {
                }.getType());
                log.info("Profile list created from file, attempting to update Profiles...");

                if (profile.getName().equals("profiles.females.json")) {
                    log.info("Creating females list...");
                    List<Profile> updatedList = profileList.stream().map(p -> {
                        if (!p.isVerified() || !p.isAi()) {
                            return new Profile(UUID.randomUUID().toString(),
                                    p.getFirstName(),
                                    p.getLastName(),
                                    p.getAge(),
                                    p.getEthnicity(),
                                    p.getGender(),
                                    p.getBio(),
                                    p.getImageUrl(),
                                    true,
                                    true,
                                    p.getMyersBriggsPersonalityType());
                        } else {
                            return p;
                        }
                    }).toList();

                    writeUpdatedJsonList(updatedList, profile.getName());

                } else {
                    log.info("Creating males list...");
                    List<Profile> updatedList = profileList.stream().map(p -> {
                        if (!p.isVerified() || !p.isAi()) {
                            return new Profile(UUID.randomUUID().toString(),
                                    p.getFirstName(),
                                    p.getLastName(),
                                    p.getAge(),
                                    p.getEthnicity(),
                                    p.getGender(),
                                    p.getBio(),
                                    p.getImageUrl(),
                                    true,
                                    true,
                                    p.getMyersBriggsPersonalityType());
                        } else {
                            return p;
                        }
                    }).toList();

                    writeUpdatedJsonList(updatedList, profile.getName());
                }
            }

            } catch(IOException io){
                log.error("Unable to process file: ", io);
            }

    }

    private void writeUpdatedJsonList(List<Profile> updatedProfiles, String fileName) {
        log.info("Attempting to convert updated profiles to a json file...");
        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<List<Profile>>() {
        }.getType();

        File file = new File(fileName + ".updated.json");

        try (FileWriter writer = new FileWriter(file);) {
            log.info("Attempting to write updated profiles back to a json file...");
            gsonWriter.toJson(updatedProfiles, type, writer);
            log.info("New json file called: [{}]", file.getName());
        } catch (IOException e) {
            log.error("Unable to process the updated profile: ", e);
        }
    }


    private void seedDataBase() {
        log.info("Attempting to seed database...");

        File[] profiles = GetProfiles();

        if (profiles == null) {
            log.error("No files in folder [{}]...", PROFILES_FOLDER);
            return;
        }

        log.info("Attempting to read updated json files and create a list of profiles...");

        try {
            for (File profile: profiles) {
                if (profile.getName().endsWith(".txt")) {
                    continue;
                }

                log.info("Processing file: [{}]", profile.getName());
                List<Profile> profileList = gson.fromJson(new FileReader(profile), new TypeToken<List<Profile>>() {
                }.getType());
                log.info("Profile list created from file, attempting to save Profiles to database...");

                profileRepo.saveAll(profileList);
                log.info("Profiles saved...");

                log.info("Attempting to create Users from Profiles...");
                List<User> userList = new ArrayList<>();
                profileList.forEach(p -> userList.add(
                        new User(
                                p.getUserId(),
                                p.getFirstName().toLowerCase().concat(p.getLastName().toLowerCase()).concat("@noemail.com"),
                                passwordEncoder.encode("1Ms#&qRL"),
                                new Date(),
                                null,
                                null,
                                null
                        ))
                );
                log.info("Users created from, attempting to save to database...");
                userRepo.saveAll(userList);
                log.info("Users saved to database...");
            }

        } catch (Exception e) {
            log.error("Unable to read json file error: \n", e);
        }

        log.info("Database seeded successfully...");
    }


    private void seedPersonalities() {

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
        log.info("Seeding complete");
    }

    private void InsertDescription(PersonalitiesTypes type, String desc) {
        descriptionRepo.insert(new PersonalityDescription(type.id(), desc));
    }

    private File[] GetProfiles() {

        log.info("Getting a list of profiles from folder: [{}]", PROFILES_FOLDER);
        File filePath = new File(PROFILES_FOLDER);

        if(!filePath.exists() || !filePath.isDirectory()){
            log.error("Folder [{}] does not exist...", PROFILES_FOLDER);
            return null;
        }

        return filePath.listFiles();
    }

}