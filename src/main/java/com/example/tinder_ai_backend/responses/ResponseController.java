package com.example.tinder_ai_backend.responses;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;


@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
public class ResponseController {

    private final static Logger logger = LogManager.getLogger(ResponseController.class);
    private final ResponseService service;

    @GetMapping(path = "/response")
    public ResponseEntity<String> getResponse(@RequestBody Response req) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(service.generateChatResponse(req).get());
    }
}
