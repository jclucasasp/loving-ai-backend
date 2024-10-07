package com.example.ai_dating_backend;

import com.example.ai_dating_backend.services.DataBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class
TinderAiBackendApplication implements CommandLineRunner {

    final DataBaseService dataBaseHelper;

   //TODO: Change the personalityTypeId() to point to the personalityTypeId in the profiles.json file and fix it in the databaseHelper file
    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {
//        dataBaseHelper.updateAndSaveProfiles();
//        dataBaseHelper.purgeData();
//        dataBaseHelper.seedDataBase();
//        dataBaseHelper.seedPersonalities();

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