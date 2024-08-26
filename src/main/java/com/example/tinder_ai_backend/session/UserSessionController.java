package com.example.tinder_ai_backend.session;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
public class UserSessionController {

    private final UserSessionRepo sessionRepo;

    @GetMapping(path = "/session/all")
    public ResponseEntity<List<UserSession>> getAllUserSessions() {
        return ResponseEntity.ok(sessionRepo.findAll());
    }

    @DeleteMapping(path = "/session/delete/all")
    public ResponseEntity<String> deleteAllSession(){
        sessionRepo.deleteAll();
        return ResponseEntity.ok("All sessions deleted");
    }
}
