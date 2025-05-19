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
        Farmer farmer = farmerService.findByEmail(auth.getName());
        return toResponseDTO(farmer);
    }
    // Update farmer
    @PutMapping("/me")
    public FarmerDetailResponse updateMyProfile(Authentication auth, @Valid @RequestBody FarmerRequest requestDTO) {
        Farmer existingFarmer = farmerService.findByEmail(auth.getName());
        existingFarmer.setName(requestDTO.name());
        existingFarmer.setEmail(requestDTO.email());
        existingFarmer.setPassword(requestDTO.password());

        Farmer updated = farmerService.update(existingFarmer.getId(), existingFarmer);
        return toResponseDTO(updated);
    }
    // Delete farmer
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(Authentication auth) {
        Farmer farmer = farmerService.findByEmail(auth.getName());
        farmerService.deleteById(farmer.getId());
        return ResponseEntity.noContent().build();
    }
    // Utility method to convert entity to response DTO
    private FarmerDetailResponse toResponseDTO(Farmer farmer) {
        return new FarmerDetailResponse(
                farmer.getId(),
                farmer.getName(),
                farmer.getEmail(),
                farmer.getHarvestRecords().stream().map(hr -> hr.getId()).toList(),
                farmer.getCrops().stream().map(c -> c.getId()).toList(),
                farmer.getFields().stream().map(f -> f.getId()).toList()
        );
    }
    // Utility method to convert request DTO to entity
    private Farmer toEntity(FarmerRequest dto) {
        Farmer farmer = new Farmer();
        farmer.setName(dto.name());
        farmer.setEmail(dto.email());
        farmer.setPassword(dto.password());


        return farmer;
    }

    @GetMapping // Optional: actually we don't need, because only role is farmer , each farmer can only get their data
    public List<FarmerBasicResponse> getAllFarmers() {
        return farmerService.getAllFarmers().stream()
                .map(f -> new FarmerBasicResponse(f.getId(), f.getName(), f.getEmail()))
                .toList();
    }
}
