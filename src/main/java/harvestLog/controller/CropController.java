package harvestLog.controller;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.service.ICropService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final ICropService cropService;

    public CropController(ICropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping
    public ResponseEntity<List<CropResponse>> getAll() {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(cropService.getAll(farmerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropResponse> getById(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return cropService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CropResponse> create(@RequestBody CropRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(cropService.create(request, farmerId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CropResponse>> createBatch(@RequestBody List<CropRequest> requests, HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        System.out.println("=== /api/crops/batch called. Authorization header present? " + (authHeader != null));
        System.out.println("Authorization: " + authHeader);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("SecurityContext Authentication: " + auth);
        Long farmerId = getAuthenticatedFarmerId(); // will log failure if any
        return ResponseEntity.ok(cropService.createBatch(requests, farmerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropResponse> update(@PathVariable Long id, @RequestBody CropRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return cropService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        boolean deleted = cropService.delete(id, farmerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/harvests")
    public ResponseEntity<List<HarvestSummaryResponse>> getHarvestsByCrop(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(cropService.getHarvestsByCrop(id, farmerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CropResponse>> searchByCategory(@RequestParam String category) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(cropService.searchByCategoryName(category, farmerId));
    }

    @GetMapping("/name-contains")
    public ResponseEntity<List<CropResponse>> searchByName(@RequestParam String s) {
        Long farmerId = getAuthenticatedFarmerId();
        return cropService.findByNameContains(s, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
