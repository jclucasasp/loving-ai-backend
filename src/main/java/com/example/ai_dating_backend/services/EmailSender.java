package com.example.ai_dating_backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Text;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

// TODO: It is not working because mailjs does not allow to send emails from servers... Need to be send from a browser
@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSender {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    String fromEmail;

    public HttpStatusCode sendEmail(String toEmail, String subject, String fullName, String message) {

        String template = "Hi " + fullName + "\n"
                + message  + "\n"
                + "Warm Regards\n"
                + "The LovingAI Team";
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            mimeMessage.setFrom(fromEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, toEmail);
            mimeMessage.setSubject(subject);
            mimeMessage.setText(template);

            mailSender.send(mimeMessage);
        } catch (MessagingException m) {
            log.error("Unable to send email: {}", m);
            return HttpStatusCode.valueOf(400);
        }

        return HttpStatusCode.valueOf(200);
    }
}