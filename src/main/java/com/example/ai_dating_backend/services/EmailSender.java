package com.example.ai_dating_backend.services;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailSender {

    @Value(value = "${spring.emailjs.service-id}")
    String service_id;

    @Value(value = "${spring.emailjs.template-id}")
    String template_id;

    @Value(value = "${spring.emailjs.user-id}")
    String user_id;

    @Value(value = "${spring.emailjs.uri}")
    String emailJSUri;

    public HttpResponse<String> sendEmail(String fullName, String message) {

        String jsonString = buildEmailBody(fullName, message);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(emailJSUri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        }
    }

    private String buildEmailBody(String fullName, String message) {

        TemplateParams templateParams = new TemplateParams(fullName, message);
        Data data = new Data(service_id, template_id, user_id, templateParams);

        Gson gson = new Gson();
        return gson.toJson(data);
    }
}

@AllArgsConstructor
@Getter
 class Data {
    private String service_id;
    private String template_id;
    private String user_id;
    private  TemplateParams template_params;
}

@AllArgsConstructor
@Getter
class TemplateParams {
    private String to_name;
    private String message;
}