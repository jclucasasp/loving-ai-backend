package com.example.tinder_ai_backend.responses;

import com.example.tinder_ai_backend.services.interfaces.ResponseServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// possible solution for running a python script for tts
// https://github.com/mezbaul-h/june?tab=readme-ov-file
// https://github.com/KoljaB/LocalAIVoiceChat/blob/main/ai_voicetalk_local.py
// this one have automatic voice/speech recognition so you don't have to press a button to record
// https://github.com/mezbaul-h/june?tab=readme-ov-file
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Tag(name = "AI Response Controller", description = "AI generated chat reply")
@RestController
public class ResponseController {

    private final static Logger logger = LogManager.getLogger(ResponseController.class);
    private final ResponseServiceInterface service;

    @Operation(
            summary = "AI Chat generation",
            description = "Start to chat with the AI sending the recipients params with the original message",
            tags = {"Response", "GET"}
    )
    @RequestMapping(path = "/response", method = RequestMethod.GET)
    public ResponseEntity<String> getResponse(@RequestBody Response req) throws ExecutionException, InterruptedException {
        CompletableFuture<String> aiResponse = service.generateChatResponse(req);
        return ResponseEntity.ok(aiResponse.get());
    }
}
