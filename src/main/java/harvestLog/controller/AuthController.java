package harvestLog.controller;

import harvestLog.dto.*;
import harvestLog.model.Farmer;
import harvestLog.security.JwtUtilityService;
import harvestLog.service.impl.FarmerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtilityService jwtService;
    @Autowired
    private FarmerService farmerService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registration /= create a farmer
    @PostMapping("/register")
    public ResponseEntity<FarmerBasicResponse> registerFarmer(
            @Valid @RequestBody FarmerRegistrationRequest registrationRequest) {

        // Check if email already exists
        if (farmerService.existsByFarmerEmail(registrationRequest.email())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already registered"
            );
        }
        // Create new farmer with encoded password
        Farmer farmer = new Farmer();
        farmer.setName(registrationRequest.name());
        farmer.setEmail(registrationRequest.email());
        farmer.setPassword(passwordEncoder.encode(registrationRequest.password()));

        Farmer savedFarmer = farmerService.create(farmer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FarmerBasicResponse(savedFarmer.getId(), savedFarmer.getName(),
                        savedFarmer.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Farmer farmer = farmerService.findByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), farmer.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(farmer.getEmail(), farmer.getId());

        System.out.println("Generated Token: " + token);

        return ResponseEntity.ok(new LoginResponse(token));
    }

}
