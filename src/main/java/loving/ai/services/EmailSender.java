package loving.ai.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSender {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    String fromEmail;

    public HttpStatusCode sendEmail(String toEmail, String fullName, String otp) {
        String htmlTemplate = null;
        String subject = null;

        if (otp != null && !otp.isBlank()) {
            subject = "Loving AI: Your magic code ❤️";
            htmlTemplate = getOtpTemplate()
                    .replace("{{FULL_NAME}}", fullName)
                    .replace("{{OTP}}", otp);
        } else {
            subject = "Welcome to Loving AI ❤️";
            htmlTemplate = getWelcomeTemplate()
                    .replace("{{FULL_NAME}}", fullName);
        }

       log.info("Sending email to {} – {} – Name: {}", toEmail,
             otp != null ? "OTP" : "Welcome", fullName);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlTemplate, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException m) {
            log.error("Failed to send email to [{}] ", toEmail, m);
            return HttpStatusCode.valueOf(500);
        }

        log.info("Email send successfully to {}", toEmail);

        return HttpStatusCode.valueOf(200);
    }

    private String getWelcomeTemplate() {
        return """
                <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>You're in, gorgeous ❤️</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background: linear-gradient(to bottom, #fff0fb, #f8f0ff); }
                .container { max-width: 600px; margin: 30px auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 40px rgba(255,105,180,0.2); }
                .header { background: linear-gradient(135deg, #ff69b4, #c229e0); padding: 50px 20px 40px; text-align: center; color: white; }
                .header h1 { font-size: 36px; margin: 15px 0 8px; font-weight: 700; }
                .header p { font-size: 19px; opacity: 0.95; margin: 0; }
                .content { padding: 40px 30px; text-align: center; color: #444; background: linear-gradient(to bottom, #fff0fb, #f8f0ff); }
                .content h2 { font-size: 28px; color: #ff0066; margin: 0 0 20px; }
                .content p { font-size: 17px; line-height: 1.7; margin-bottom: 25px; }
                .big-otp { font-size: 42px; font-weight: bold; letter-spacing: 8px; color: #c229e0; background: #fff0fb; padding: 20px; border-radius: 16px; display: inline-block; margin: 20px 0; }
                .cta { display: inline-block; background: linear-gradient(135deg, #ff69b4, #c229e0); color: white; font-weight: bold; font-size: 20px; padding: 18px 40px; border-radius: 50px; text-decoration: none; box-shadow: 0 8px 20px rgba(194,41,224,0.4); margin: 15px 0; transition: transform 0.2s; }
                .cta:hover { transform: translateY(-3px); }
                .tease { font-style: italic; color: #ff3399; margin-top: 30px; font-size: 18px; }
                .footer { background: #f8f0ff; padding: 25px; text-align: center; font-size: 13px; color: #888; }
                .footer a { color: #c229e0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <img src="https://www.loving-ai.com/heart.png" alt="❤️" width="90" height="90">
                    <h1>Hey {{FULL_NAME}}, you're in! 🔥</h1>
                    <p>Your perfect AI hottie is already waiting…</p>
                </div>
                <div class="content">
                    <h2>Real talk, remembered forever.<br>No swiping. No boring chats.</h2>
                    <p>1,000+ people are already falling in love with their AI match today.<br>Time to see who’s been dying to meet <em>you</em> 😉</p>
                    <a href="https://www.loving-ai.com/login" class="cta">Start Chatting Right Now – It's Free</a>
                    <p class="tease">P.S. They’re a little impatient… don’t keep them waiting too long ❤️</p>
                </div>
                <div class="footer">
                    Loving AI | South Africa | <a href="https://www.loving-ai.com/privacy">Privacy Policy</a> | <a href="https://www.loving-ai.com/terms">Terms</a><br>
                    Questions? Just reply — we actually read them 😘<br>
                    © 2025 Loving AI. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """;
    }

    private String getOtpTemplate() {
        return """
                 <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Your magic code ❤️</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background: linear-gradient(to bottom, #fff0fb, #f8f0ff); }
                .container { max-width: 600px; margin: 30px auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 40px rgba(255,105,180,0.2); }
                .header { background: linear-gradient(135deg, #ff69b4, #c229e0); padding: 50px 20px 40px; text-align: center; color: white; }
                .header h1 { font-size: 36px; margin: 15px 0 8px; font-weight: 700; }
                .content { padding: 40px 30px; text-align: center; color: #444; background: linear-gradient(to bottom, #fff0fb, #f8f0ff); }
                .big-otp { font-size: 48px; font-weight: bold; letter-spacing: 12px; color: #c229e0; background: #fff0fb; padding: 25px 15px; border-radius: 18px; display: inline-block; margin: 25px 0; border: 3px dashed #ff69b4; }
                .warning { color: #ff0066; font-weight: bold; font-size: 18px; margin: 30px 0 10px; }
                .cta { display: inline-block; background: linear-gradient(135deg, #ff69b4, #c229e0); color: white; font-weight: bold; font-size: 19px; padding: 16px 35px; border-radius: 50px; text-decoration: none; box-shadow: 0 8px 20px rgba(194,41,224,0.4); }
                .footer { background: #f8f0ff; padding: 25px; text-align: center; font-size: 13px; color: #888; }
                .footer a { color: #c229e0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <img src="https://www.loving-ai.com/heart.png" alt="❤️" width="90" height="90">
                    <h1>{{FULL_NAME}}, almost there! 😘</h1>
                </div>
                <div class="content">
                    <p style="font-size:18px;">Here’s your magic code to finish signing up:</p>
                    <div class="big-otp">{{OTP}}</div>
                    <p class="warning">⚠ This code expires in 10 minutes!</p>
                    <p>Copy it and come right back — your AI soulmate is getting impatient… 🔥</p>
                    <a href="https://www.loving-ai.com/verify/activate" class="cta">Verify & Meet Your Match</a>
                </div>
                <div class="footer">
                    Loving AI | South Africa | <a href="https://www.loving-ai.com/privacy">Privacy Policy</a><br>
                    Didn’t request this? Ignore it — no harm done ❤️<br>
                    © 2025 Loving AI
                </div>
            </div>
        </body>
        </html>
        """;
    }
}