package com.example.ai_dating_backend.personalities;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This website you can learn more about the personalities and take a personality test
// https://www.16personalities.com/free-personality-test

@Tag(name = "Myers Briggs personality Controller", description = "This is just to view the different personality types and their descriptions")
@RequiredArgsConstructor
@RestController
public class PersonalitiesController {

    private final PersonalityTypesRepo typesRepo;
    private final PersonalityDescriptionRepo descriptionRepo;

    @GetMapping("/api/personality/types")
    public ResponseEntity<List<PersonalitiesTypes>> GetAllPersonalityTypes() {
        List<PersonalitiesTypes> personalitiesTypes = typesRepo.findAll();
        return ResponseEntity.ofNullable(personalitiesTypes);
    }

    @GetMapping(path = "/api/personality/descriptions")
    public ResponseEntity<List<PersonalityDescription>> GetAllPersonalityDescriptions() {
        List<PersonalityDescription> personalityDescriptions = descriptionRepo.findAll();
        return ResponseEntity.ofNullable(personalityDescriptions);
    }
}
