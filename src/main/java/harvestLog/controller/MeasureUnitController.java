package harvestLog.controller;

import harvestLog.dto.BatchActiveRequest;
import harvestLog.dto.HardDeleteRequest;
import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.service.IMeasureUnitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/measure-units")
public class MeasureUnitController {

    private final IMeasureUnitService measureUnitService;

    public MeasureUnitController(IMeasureUnitService measureUnitService) {
        this.measureUnitService = measureUnitService;
    }

    @GetMapping
    public ResponseEntity<List<MeasureUnitResponse>> getAllMeasureUnits(
            @RequestParam(required = false) Boolean active
    ) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(measureUnitService.getAllForFarmerId(farmerId, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeasureUnitResponse> getMeasureUnit(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return measureUnitService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MeasureUnitResponse> create(@Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnitResponse created = measureUnitService.create(request, farmerId);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeasureUnitResponse> update(@PathVariable Long id, @Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return measureUnitService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        boolean deleted = measureUnitService.delete(id, farmerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<List<MeasureUnitResponse>> createBatch(@Valid @RequestBody List<MeasureUnitRequest> requests) {
        Long farmerId = getAuthenticatedFarmerId();
        List<MeasureUnitResponse> created = measureUnitService.createBatch(requests, farmerId);
        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        Long farmerId = getAuthenticatedFarmerId();
        int deletedCount = measureUnitService.deleteBatch(ids, farmerId);
        return deletedCount > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/batch/hard")
    public ResponseEntity<Void> hardDeleteBatch(@RequestBody HardDeleteRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        measureUnitService.hardDeleteBatch(request.ids(), farmerId, request.cascade());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/batch/active")
    public ResponseEntity<Void> updateActiveBatch(@RequestBody BatchActiveRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        measureUnitService.updateActiveBatch(request.ids(), farmerId, request.active());
        return ResponseEntity.noContent().build();
    }
}
