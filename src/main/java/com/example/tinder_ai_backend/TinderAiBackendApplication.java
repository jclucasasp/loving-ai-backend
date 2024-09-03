package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.lib.DataBaseService;
import com.example.tinder_ai_backend.responses.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SpringBootApplication
@RequiredArgsConstructor
public class TinderAiBackendApplication implements CommandLineRunner {


    private final DataBaseService dataBaseHelper;
    private final ResponseService responseService;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {
//        dataBaseHelper.purgeData();
//        dataBaseHelper.seedDataBase();
        Future<String> initial = responseService.generateChatResponse(new Prompt("Please feel free to use as much emojis and humour as you like slight smile"));
        Future<String> future = responseService.generateChatResponse(new Prompt("Hows you going shiela?"));
        Future<String> future1 = responseService.generateChatResponse(new Prompt("Whats your name?"));
        Future<String> future2 = responseService.generateChatResponse(new Prompt("What can you do?"));

        Future<String> future4 = responseService.generateChatResponse(new Prompt("Do you want me to penetrate you?"));
        Future<String> future5 = responseService.generateChatResponse(new Prompt("I love you!"));
        Future<String> future3 = responseService.generateChatResponse(new Prompt("Are you human?"));

        try {
            System.out.println(initial.get());
            System.out.println(future.get());
            System.out.println(future1.get());
            System.out.println(future2.get());
            System.out.println(future3.get());
            System.out.println(future4.get());
            System.out.println(future5.get());

        } catch (InterruptedException | ExecutionException e) {
            System.err.println(e);
        }

//        int count = 0;
//        while(count < 50 ) {
//            try {
//                Future<String> future = responseService.generateChatResponse(new Prompt("What can you do?"));
//                System.out.println(future.get());
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            count++;
//            System.out.println("Loop number: [ "+count+" ]");
//        }
    }
}