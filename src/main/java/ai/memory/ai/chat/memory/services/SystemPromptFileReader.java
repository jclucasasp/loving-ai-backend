package ai.memory.ai.chat.memory.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class SystemPromptFileReader {

    @Value("${spring.json.profile.folder.name}")
    private String PROFILES_FOLDER;

    public String readJsonFile() {

        File filePath = new File(PROFILES_FOLDER.concat("/sprompt.txt"));

        if (!filePath.exists()) {
            log.error("Folder specified in the properties file does not exist or is empty: [{}]", filePath);
            return null;
        }

        try {
            return Files.readString(filePath.toPath());
        } catch (IOException e) {
            log.error("Unable to read file [{}]", filePath.toPath());
            return null;
        }
    }
}
