package harvestLog.controller;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.model.Category;
import harvestLog.service.ICropService;
import harvestLog.service.IFarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    @Autowired
    private ICropService cropService;

    @PostMapping
    public ResponseEntity<CropResponse> createCrop(@RequestBody CropRequest dto) {
        return ResponseEntity.ok(cropService.addCrop(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CropResponse>> createCrops(@RequestBody List<CropRequest> cropRequests) {
    List<CropResponse> created = cropService.addCrops(cropRequests);
    return ResponseEntity.ok(created);
}


    @GetMapping
    public ResponseEntity<List<CropResponse>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/{id}/harvests")
    public ResponseEntity<List<HarvestSummaryResponse>> getHarvestsByCrop(@PathVariable Long id) {
        return ResponseEntity.ok(cropService.getHarvestsByCrop(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropResponse> getCropById(@PathVariable Long id) {
        return ResponseEntity.ok(cropService.getCropById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CropResponse>> searchCropsByCategory(@RequestParam String category) {
        return ResponseEntity.ok(cropService.searchByCategoryName(category));
    }

    @GetMapping("/name-contains")
    public ResponseEntity<List<CropResponse>> searchCropsByName(@RequestParam String s) {
        return cropService.findByNameContains(s).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropResponse> updateCrop(@PathVariable Long id, @RequestBody CropRequest dto) {
        return ResponseEntity.ok(cropService.updateCrop(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable Long id) {
        cropService.deleteCrop(id);
        return ResponseEntity.noContent().build();
    }
}
