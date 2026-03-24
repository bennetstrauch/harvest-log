package harvestLog.controller;

import harvestLog.dto.AdminPlanUpdateRequest;
import harvestLog.dto.FarmerBasicResponse;
import harvestLog.service.IFarmerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final IFarmerService farmerService;

    @Value("${app.admin.email:}")
    private String adminEmail;

    public AdminController(IFarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PatchMapping("/farmers/{email}/plan")
    public ResponseEntity<FarmerBasicResponse> updateFarmerPlan(
            Authentication auth,
            @PathVariable String email,
            @Valid @RequestBody AdminPlanUpdateRequest request) {

        if (adminEmail.isBlank() || !auth.getName().equals(adminEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FarmerBasicResponse updated = farmerService.updatePlan(email, request.planType());
        return ResponseEntity.ok(updated);
    }
}
