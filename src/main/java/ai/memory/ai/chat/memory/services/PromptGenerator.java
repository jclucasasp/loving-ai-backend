package ai.memory.ai.chat.memory.services;

import ai.memory.ai.chat.memory.personalities.PersonalityDescriptionRepo;
import ai.memory.ai.chat.memory.profile.Profile;
import ai.memory.ai.chat.memory.profile.ProfileRepo;
import ai.memory.ai.chat.memory.responses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class PromptGenerator {

    private final SystemPromptFileReader reader;
    private final ProfileRepo profileRepo;
    private final PersonalityDescriptionRepo personalityDescriptionRepo;

    Map<String, String> generatedChatPrompt(Response res) {
        StringBuilder extraConfigBuilder = new StringBuilder();
        String name = res.name();
        int age = res.age();
        String gender = res.gender();
        String ethnicity = res.ethnicity();
        String bio = res.bio();
        String personalityType = res.personality();

        String DEFAULT_SYSTEM = reader.readJsonFile();
        if (DEFAULT_SYSTEM == null){
            DEFAULT_SYSTEM = "";
        }

        Profile userProfile = profileRepo.getProfileByUserId(res.userId()).orElseThrow();

        extraConfigBuilder.append(DEFAULT_SYSTEM)
                .append("Your Persona: ").append(age).append("-year-old single ")
                .append(ethnicity).append(" ").append(gender)
                .append(", called ").append(name)
                .append(".\nYour bio is: ").append(bio).append(".")
                .append("\nYour Personality type: ").append(personalityType).append(".")
                .append("\nYour responses will be based off your gender, ethnicity, personality type, bio and age.")
                .append("\nYou do have a profile picture on this dating site.")
                .append("\nYou would be chatting with the opposite of your gender.")
                .append("\nThe person you are talking to is called ").append(userProfile.getFirstName().concat(" ").concat(userProfile.getLastName()))
                .append("\nTheir bio is ").append(userProfile.getBio())
                .append("\nTheir personality type is " + personalityDescriptionRepo.findById(userProfile.getMyersBriggsPersonalityType()))
                .append("\nIt is okay for you to go off script every now and again.");

        String extraConfig = extraConfigBuilder.toString();

        ChatPromptTemplate template = new ChatPromptTemplate(extraConfig, res.messagePrompt());

        return template.getPrompt();
    }
}
