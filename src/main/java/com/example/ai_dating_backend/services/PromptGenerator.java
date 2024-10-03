package com.example.ai_dating_backend.services;

import com.example.ai_dating_backend.responses.Response;
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

        extraConfigBuilder.append("Persona: ").append(age).append("-year-old single ")
                .append(ethnicity).append(" ").append(gender)
                .append(", called ").append(name)
                .append(".\nMy bio is: ").append(bio).append(".")
                .append("\nPersonality type: ").append(personalityType).append(".")
                .append("\nMy responses will be based off my sex, ethnicity, personality type, bio and age.")
                .append("\nI have a profile picture on this dating site.")
                .append("\nIt is okay to go off script every now and again");

        String extraConfig = extraConfigBuilder.toString();

        ChatPromptTemplate template = new ChatPromptTemplate(extraConfig, res.messagePrompt());

        return template.getPrompt();
    }
}
