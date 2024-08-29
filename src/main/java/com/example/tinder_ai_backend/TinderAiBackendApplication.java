package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.lib.DataBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TinderAiBackendApplication implements CommandLineRunner {

    //    private final OllamaChatModel chatClient;
    private final DataBaseService dataBaseHelper;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {
//        dataBaseHelper.purgeData();
//        dataBaseHelper.seedDataBase();
//        Prompt prompt = new Prompt("Who are you?");
//        ChatResponse call = chatClient.call(prompt);
//        String content = call.getResult().getOutput().getContent();
//        System.out.println(content);
    }
}