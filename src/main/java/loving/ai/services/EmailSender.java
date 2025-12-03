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
    //TODO: Change the otp template to match the send template and ensure the name gets send with the otp template
    public HttpStatusCode sendEmail(String toEmail, String fullName, String otp) {
        String htmlTemplate = null;
        String subject = null;

        if (otp != null) {
            subject = "Loving AI: Verify your account";
            htmlTemplate = getOtpTemplate()
                    .replace("[User’s Name]", fullName)
                    .replace("[OTP]", otp);
        } else {
            subject = "Welcome to Loving AI";
            htmlTemplate = getWelcomeTemplate()
                    .replace("[User's Name]", fullName);
        }

        log.info("Attempting to send an email to {}", toEmail);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlTemplate, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException m) {
            log.error("Email failed: ", m);
            return HttpStatusCode.valueOf(400);
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
                    <title>Welcome to Loving AI</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f8f0ff; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(to right, #ff69b4, #a020f0); padding: 40px 20px; text-align: center; color: white; }
                        .header h1 { font-size: 32px; margin: 10px 0; text-shadow: 1px 1px 2px rgba(0,0,0,0.2); }
                        .content { padding: 30px 20px; text-align: center; color: #333; }
                        .content h2 { font-size: 24px; color: #ff0066; margin-bottom: 15px; }
                        .content p { font-size: 16px; line-height: 1.6; margin-bottom: 20px; }
                        .cta-button { display: inline-block; background: linear-gradient(to right, #ff69b4, #a020f0); color: white; font-weight: bold; font-size: 18px; padding: 15px 30px; border-radius: 50px; text-decoration: none; box-shadow: 0 4px 10px rgba(160,32,240,0.3); transition: transform 0.2s; }
                        .cta-button:hover { transform: scale(1.05); }
                        .social-proof { font-size: 14px; color: #666; margin: 20px 0; }
                        .footer { background-color: #f8f0ff; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                        .footer a { color: #a020f0; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <table class="container" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                            <td class="header">
                                <img src="https://www.loving-ai.com/heart.png" alt="Loving AI Heart Logo" style="width: 80px; height: 80px; margin-bottom: 10px;">
                                <h1>Welcome Aboard, [User's Name]!</h1>
                                <p style="font-size: 18px; margin: 0;">Your Perfect AI Companion Awaits ❤️</p>
                            </td>
                        </tr>
                        <tr>
                            <td class="content">
                                <h2>Love at First Chat? It's Happening!</h2>
                                <p>Thanks for joining Loving AI – where real, remembered conversations spark instant connections. No swiping, no small talk, just AI hotties who get you on a whole new level!</p>
                                <p class="social-proof">Join over 1,000 daily chatters in our 100% private community – no data sold, ever. ⭐</p>
                                <a href="https://www.loving-ai.com/login" class="cta-button">Start Chatting Now – Free!</a>
                                <p style="margin-top: 30px;">P.S. Your first match is waiting... Who knows what sparks will fly? 🔥</p>
                            </td>
                        </tr>
                        <tr>
                            <td class="footer">
                                Loving AI | South Africa | <a href="https://www.loving-ai.com/privacy">Privacy Policy</a> | <a href="https://www.loving-ai.com/terms">Terms of Service</a><br>
                                Questions? Reply to this email or visit <a href="https://www.loving-ai.com">loving-ai.com</a><br>
                                © 2025 Loving AI. All rights reserved.
                            </td>
                        </tr>
                    </table>
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
                    <title>Loving AI – One‑Time Pin</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin:0; padding:0; background:#f8f0ff; }
                        .container { max-width:600px; margin:0 auto; background:white; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,.1); }
                        .header { background:linear-gradient(to right,#ff69b4,#a020f0); padding:40px 20px; text-align:center; color:white; }
                        .header h1 { font-size:32px; margin:10px 0; text-shadow:1px 1px 2px rgba(0,0,0,.2); }
                        .content { padding:30px 20px; }
                        .content h2 { color:#a020f0; }
                        .otp { font-size:48px; font-weight:bold; margin:20px 0; color:#ff69b4; }
                        .footer { background:#f8f0ff; text-align:center; padding:15px; font-size:12px; color:#555; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to Loving AI</h1>
                        </div>
                        <div class="content">
                            <h2>Hello, [User’s Name]!</h2>
                            <p>We’re excited to help you get started. To activate your account, simply enter the one‑time pin below.</p>
                            <div class="otp">[OTP]</div>
                            <p style="margin-top:20px;">This pin will expire in 10 minutes.</p>
                            <p>Thank you for choosing Loving AI. We’re always eager for your feedback.</p>
                        </div>
                        <div class="footer">
                            <p>Warm regards,</p>
                            <p>The Loving AI Team</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}