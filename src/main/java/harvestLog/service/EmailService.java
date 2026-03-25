package harvestLog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; background: #f9fafb; border-radius: 8px;">
                  <h2 style="color: #111827; margin-bottom: 8px;">Verify your CropLog account</h2>
                  <p style="color: #6b7280; margin-bottom: 24px;">Click the button below to confirm your email address. The link expires in 24 hours.</p>
                  <a href="%s"
                     style="display: inline-block; background: #0f766e; color: #ffffff; text-decoration: none;
                            padding: 12px 24px; border-radius: 6px; font-weight: 600;">
                    Verify Email
                  </a>
                  <p style="color: #9ca3af; font-size: 12px; margin-top: 24px;">If you didn't create a CropLog account, you can ignore this email.</p>
                </div>
                """.formatted(verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject("Verify your CropLog account");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email to " + to, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; background: #f9fafb; border-radius: 8px;">
                  <h2 style="color: #111827; margin-bottom: 8px;">Reset your CropLog password</h2>
                  <p style="color: #6b7280; margin-bottom: 24px;">Click the button below to set a new password. The link expires in 1 hour.</p>
                  <a href="%s"
                     style="display: inline-block; background: #0f766e; color: #ffffff; text-decoration: none;
                            padding: 12px 24px; border-radius: 6px; font-weight: 600;">
                    Reset Password
                  </a>
                  <p style="color: #9ca3af; font-size: 12px; margin-top: 24px;">If you didn't request a password reset, you can ignore this email.</p>
                </div>
                """.formatted(resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject("Reset your CropLog password");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email to " + to, e);
        }
    }
}
