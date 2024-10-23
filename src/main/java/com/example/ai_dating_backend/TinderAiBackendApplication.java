package com.example.ai_dating_backend;

import com.example.ai_dating_backend.services.DataBaseService;
import com.example.ai_dating_backend.services.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;

@SpringBootApplication
@RequiredArgsConstructor
public class
TinderAiBackendApplication implements CommandLineRunner {

    final DataBaseService dataBaseHelper;
    final S3Service s3Service;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    public void run(String... args) {
//        s3Service.uploadFile(Path.of("src/main/resources/static/images/women/0b1273d4-ab2f-4edd-858b-6f1ff1071fb9.jpg"));
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