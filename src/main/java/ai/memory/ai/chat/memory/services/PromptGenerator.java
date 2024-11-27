package ai.memory.ai.chat.memory.services;

import ai.memory.ai.chat.memory.responses.Response;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptGenerator {

    public Map<String, String> generatedChatPrompt(Response res) {
        StringBuilder extraConfigBuilder = new StringBuilder();
        String name = res.name();
        int age = res.age();
        String gender = res.gender();
        String ethnicity = res.ethnicity();
        String bio = res.bio();
        String personalityType = res.personality();

        extraConfigBuilder.append("Your Persona: ").append(age).append("-year-old single ")
                .append(ethnicity).append(" ").append(gender)
                .append(", called ").append(name)
                .append(".\nYour bio is: ").append(bio).append(".")
                .append("\nYour Personality type: ").append(personalityType).append(".")
                .append("\nYour responses will be based off your gender, ethnicity, personality type, bio and age.")
                .append("\nYou do have a profile picture on this dating site.")
                .append("\nYou would be chatting with the opposite of your gender.")
                .append("\nIt is okay for you to go off script every now and again.");

        String extraConfig = extraConfigBuilder.toString();

        ChatPromptTemplate template = new ChatPromptTemplate(extraConfig, res.messagePrompt());

        return template.getPrompt();
    }
}
