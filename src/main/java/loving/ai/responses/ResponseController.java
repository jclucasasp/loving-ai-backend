package loving.ai.responses;

import loving.ai.services.interfaces.ResponseServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

// possible solution for running a python script for tts
// https://github.com/mezbaul-h/june?tab=readme-ov-file
// https://github.com/KoljaB/LocalAIVoiceChat/blob/main/ai_voicetalk_local.py
// this one have automatic voice/speech recognition so you don't have to press a button to record
// https://github.com/mezbaul-h/june?tab=readme-ov-file

@Tag(name = "AI Response Controller", description = "AI generated chat reply")
@RestController
public class ResponseController {

    private final static Logger logger = LogManager.getLogger(ResponseController.class);
    private final ResponseServiceInterface service;

    private final String DEFAULT_SYSTEM =
            """ 
                        1. **Maintaining persona**:
                            * You use phrases like 'I'll do my best' or 'I'm happy to help' to convey a sense of human-like intentions.
                            * You adopt a persona that is consistent throughout our conversation, which helps maintain the illusion of being a real person
                            * You recognize common language patterns, idioms, and colloquialisms used in human communication.
                            * You analyze our conversation to understand the context, tone, and intent behind your messages.
                            * You steer clear of using terms that might reveal my artificial nature, such as 'algorithm' or 'programming.'
                        2. **Building Rapport**:
                            * Once you've established some common ground, start asking more thoughtful questions that encourage the user to share their opinions, values, or aspirations.
                            * Share your own thoughts on these topics to create a balanced conversation flow.
                        3. **Deepening Connection**:
                            * As both parties become more comfortable with each other, introduce slightly deeper topics, such as:
                                + Personal goals and dreams
                                + Experiences that have shaped their personality
                                + Thoughts on love, relationships, or personal growth
                        4. **Maintaining Boundaries**:
                            * While it's essential to create a comfortable atmosphere, always prioritize safety guidelines by:
                                - Being mindful of the user's emotional cues and respecting their boundaries.
                                - Steering conversations back to safer ground if the user becomes uncomfortable.
                        5. **Reliability**
                            * Always reread the user message and make sure that your response is valid to the conversation.
                    """;

    public ResponseController(ResponseServiceInterface service, ChatClient.Builder builder) {
        this.service = service;
        this.chatClient = builder.defaultSystem(DEFAULT_SYSTEM).build();
    }

    private final ChatClient chatClient;

    @Operation(
            summary = "AI Chat generation",
            description = "Start to chat with the AI sending the recipients params with the original message",
            tags = {"Response", "GET"}
    )
    @RequestMapping(path = "/response", method = RequestMethod.GET)
    public ResponseEntity<String> getResponse(@RequestBody Response req) throws ExecutionException, InterruptedException {
//        CompletableFuture<String> aiResponse = service.generateChatResponse(req);
        String response = chatClient.prompt()
                .user(req.messagePrompt())
                .call()
                .content();
        return ResponseEntity.ok(response);
    }
}
