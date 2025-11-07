package ai.memory.ai.chat.memory.personalities;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This website you can learn more about the personalities and take a personality test
// https://www.16personalities.com/free-personality-test
@Log4j2
@Tag(name = "Myers Briggs personality Controller", description = "This is just to view the different personality types and their descriptions")
@RequiredArgsConstructor
@RestController
public class PersonalitiesController {

    private final PersonalityTypesRepo typesRepo;
    private final PersonalityDescriptionRepo descriptionRepo;
    HttpHeaders headers = new HttpHeaders();

    @GetMapping("/api/personality/types")
    public ResponseEntity<List<PersonalitiesTypes>> GetAllPersonalityTypes() {
        List<PersonalitiesTypes> personalitiesTypes = typesRepo.findAll();
        headers.setCacheControl("public, max-age=2592000");
        return new ResponseEntity<>(personalitiesTypes, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/api/personality/descriptions")
    public ResponseEntity<List<PersonalityDescription>> GetAllPersonalityDescriptions() {
        List<PersonalityDescription> personalityDescriptions = descriptionRepo.findAll();
        headers.setCacheControl("public, max-age=2592000");
        return new ResponseEntity<>(personalityDescriptions, headers, HttpStatus.OK);
    }
}
