package loving.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient chatClient ) {
        this.chatClient = chatClient;
    }

    @GetMapping (path = "/")
    public String message(@RequestParam String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
