package com.example.tinder_ai_backend;

import com.example.tinder_ai_backend.lib.DataBaseService;
import com.example.tinder_ai_backend.responses.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.regex.Pattern;

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
//        String res = responseService.getResponse("Tell me more about yourself?");
//        System.out.println(res);
    }
}