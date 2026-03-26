package harvestLog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailService(@Value("${app.mail.resend-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

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

        send(to, "Verify your CropLog account", html);
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

        send(to, "Reset your CropLog password", html);
    }

    private void send(String to, String subject, String html) {
        try {
            var body = new EmailRequest(fromAddress, List.of(to), subject, html);
            restClient.post()
                    .uri("/emails")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private record EmailRequest(String from, List<String> to, String subject, String html) {}
}
