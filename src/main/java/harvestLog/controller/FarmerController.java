package harvestLog.controller;

import harvestLog.dto.FarmerBasicResponse;
import harvestLog.dto.FarmerRequest;
import harvestLog.dto.FarmerDetailResponse;
import harvestLog.model.Farmer;
import harvestLog.service.IFarmerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {
    @Autowired
    private IFarmerService farmerService;

    @GetMapping("/me")
    public FarmerDetailResponse getMyProfile(Authentication auth) {
        return farmerService.getMyProfile(auth.getName());
    }

    // Update farmer
    @PutMapping("/me")
    public FarmerDetailResponse updateMyProfile(Authentication auth, @Valid @RequestBody FarmerRequest requestDTO) {
        return farmerService.updateMyProfile(auth.getName(), requestDTO);
    }

    // Delete farmer
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(Authentication auth) {
        farmerService.deleteMyAccount(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping // Optional: actually we don't need, because only role is farmer , each farmer can only get their data
    public List<FarmerBasicResponse> getAllFarmers() {
        return farmerService.getAllFarmers();
    }
}
