package com.example.tinder_ai_backend.responses;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.Future;

@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
public class ResponseController {

    private final static Logger logger =  LogManager.getLogger(ResponseController.class);
    private final ResponseService service;

    @GetMapping(path = "/response")
    public ResponseEntity<String> getResponse(@RequestBody Response req) {
        Future<String> future = service.generateChatResponse(new Prompt(req.messagePrompt()));
        try {
            return  ResponseEntity.ok(future.get());
        } catch (Exception e) {
            logger.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
