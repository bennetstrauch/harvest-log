package harvestLog.controller;
//######## develop frontend ui to test backend...
import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.service.ICropService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final ICropService cropService;

    public CropController(ICropService cropService) {
        this.cropService = cropService;
    }

    @GetMapping
    public ResponseEntity<List<CropResponse>> getAll(@RequestParam Long farmerId) {
        return ResponseEntity.ok(cropService.getAll(farmerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropResponse> getById(@PathVariable Long id, @RequestParam Long farmerId) {
        return cropService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CropResponse> create(@RequestBody CropRequest request, @RequestParam Long farmerId) {
        return ResponseEntity.ok(cropService.create(request, farmerId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CropResponse>> createBatch(@RequestBody List<CropRequest> requests, @RequestParam Long farmerId) {
        return ResponseEntity.ok(cropService.createBatch(requests, farmerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropResponse> update(@PathVariable Long id, @RequestBody CropRequest request, @RequestParam Long farmerId) {
        return cropService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long farmerId) {
        boolean deleted = cropService.delete(id, farmerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/harvests")
    public ResponseEntity<List<HarvestSummaryResponse>> getHarvestsByCrop(@PathVariable Long id, @RequestParam Long farmerId) {
        return ResponseEntity.ok(cropService.getHarvestsByCrop(id, farmerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CropResponse>> searchByCategory(@RequestParam String category, @RequestParam Long farmerId) {
        return ResponseEntity.ok(cropService.searchByCategoryName(category, farmerId));
    }

    @GetMapping("/name-contains")
    public ResponseEntity<List<CropResponse>> searchByName(@RequestParam String s, @RequestParam Long farmerId) {
        return cropService.findByNameContains(s, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
