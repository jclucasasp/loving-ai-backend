package com.example.ai_dating_backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSender {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    String fromEmail;

    public HttpStatusCode sendEmail(String toEmail, String subject, String fullName, String message) {

        String template = "Hi " + fullName + "\n\n"
                + message + "\n\n"
                + "Warm Regards\n"
                + "The LovingAI Team";
        log.info("Attempting to send an email to {}", toEmail);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            mimeMessage.setFrom(fromEmail);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, toEmail);
            mimeMessage.setSubject(subject);
            mimeMessage.setText(template);

            mailSender.send(mimeMessage);
        } catch (MessagingException m) {
            log.error("Email failed: ", m);
            return HttpStatusCode.valueOf(400);
        }

        log.info("Email send successfully to {}", toEmail);

        return HttpStatusCode.valueOf(200);
    }
}