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
    @Autowired
    private IFarmerService farmerService;

    @PostMapping
    public ResponseEntity<CropResponse> createCrop(@RequestBody CropRequest dto) {
        return ResponseEntity.ok(cropService.addCrop(dto));
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
        Category cat = Category.valueOf(category.toUpperCase());
        return ResponseEntity.ok(cropService.searchByCategory(cat));
    }
    @GetMapping("/name-contains")
    public ResponseEntity<List<CropResponse>> searchCropsByName(@RequestParam String s) {
        return ResponseEntity.ok(cropService.findByNameContains(s));
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
