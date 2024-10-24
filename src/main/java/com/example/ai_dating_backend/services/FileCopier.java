package com.example.ai_dating_backend.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
@Service
public class FileCopier {

    public boolean createFile(MultipartFile file, String fileName, String gender) {

        if (file.isEmpty() || gender.isEmpty()) {
            log.error("No file or gender to use...");
            return false;
        }

        if (gender.equals("female")) {
            gender = "women";
        } else {
            gender = "men";
        }

        try {
            log.info("Attempting to save image [{}] to disk...",file.getName());
            Files.copy(file.getInputStream(),Path.of("images/",gender,"/",fileName));
        } catch (IOException io) {
            log.error("Unable to write file with exception: ", io);
            return false;
        }
        log.info("Image file successfully written to disk...");
        return true;
    }
}
