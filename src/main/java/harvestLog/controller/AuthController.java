package harvestLog.controller;

import harvestLog.dto.*;
import harvestLog.model.Farmer;
import harvestLog.repository.FarmerRepository;
import harvestLog.security.JwtUtilityService;
import harvestLog.service.EmailService;
import harvestLog.service.impl.FarmerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtilityService jwtService;
    @Autowired
    private FarmerService farmerService;
    @Autowired
    private FarmerRepository farmerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<FarmerBasicResponse> registerFarmer(
            @Valid @RequestBody FarmerRegistrationRequest registrationRequest) {

        if (farmerService.existsByFarmerEmail(registrationRequest.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        String token = UUID.randomUUID().toString();

        Farmer farmer = new Farmer();
        farmer.setName(registrationRequest.name());
        farmer.setEmail(registrationRequest.email());
        farmer.setPassword(passwordEncoder.encode(registrationRequest.password()));
        farmer.setEmailVerified(false);
        farmer.setVerificationToken(token);
        farmer.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        farmer.setTrialEndsAt(LocalDateTime.now().plusDays(90));

        Farmer savedFarmer = farmerService.create(farmer);
        try {
            emailService.sendVerificationEmail(savedFarmer.getEmail(), token);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", savedFarmer.getEmail(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FarmerBasicResponse(savedFarmer.getId(), savedFarmer.getName(), savedFarmer.getEmail()));
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        Farmer farmer = farmerRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (farmer.isEmailVerified()) {
            return ResponseEntity.ok().build();
        }

        if (farmer.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        farmer.setEmailVerified(true);
        farmerRepository.save(farmer);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody ResendVerificationRequest request) {
        Farmer farmer = farmerRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found with that email"));

        if (farmer.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already verified");
        }

        String token = UUID.randomUUID().toString();
        farmer.setVerificationToken(token);
        farmer.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        farmerRepository.save(farmer);

        try {
            emailService.sendVerificationEmail(farmer.getEmail(), token);
        } catch (Exception e) {
            log.warn("Failed to resend verification email to {}: {}", farmer.getEmail(), e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        farmerRepository.findByEmail(request.email()).ifPresent(farmer -> {
            String token = UUID.randomUUID().toString();
            farmer.setVerificationToken(token);
            farmer.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
            farmerRepository.save(farmer);
            try {
                emailService.sendPasswordResetEmail(farmer.getEmail(), token);
            } catch (Exception e) {
                log.warn("Failed to send password reset email to {}: {}", farmer.getEmail(), e.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        Farmer farmer = farmerRepository.findByVerificationToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link"));

        if (farmer.getVerificationTokenExpiry() == null ||
                farmer.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link");
        }

        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        farmer.setPassword(passwordEncoder.encode(request.newPassword()));
        farmer.setVerificationToken(null);
        farmer.setVerificationTokenExpiry(null);
        farmerRepository.save(farmer);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Farmer farmer = farmerService.findByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), farmer.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!farmer.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified. Please check your inbox.");
        }

        String token = jwtService.generateToken(farmer.getEmail(), farmer.getId());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
