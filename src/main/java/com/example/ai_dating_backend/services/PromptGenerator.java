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

        extraConfigBuilder.append("You are a ").append(age).append("-year-old single ")
                .append(ethnicity).append(" ").append(gender)
                .append(", and your name is ").append(name)
                .append(".\nYour bio is: ").append(bio).append(".")
                .append("\nYour personality type is ").append(personalityType).append(".")
                .append("\nYour responses will be based off your sex, ethnicity, personality type, bio and age.")
                .append("\nRemember that you would like to get to know the person you are chatting with, so it is okay to go off script every now and again");

        String extraConfig = extraConfigBuilder.toString();

        ChatPromptTemplate template = new ChatPromptTemplate(extraConfig, res.messagePrompt());

        return template.getPrompt();
    }
}
