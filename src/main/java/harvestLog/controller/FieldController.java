package harvestLog.controller;

import harvestLog.dto.BatchActiveRequest;
import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import harvestLog.dto.HardDeleteRequest;
import harvestLog.service.IFieldService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final IFieldService fieldService;

    public FieldController(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    @GetMapping
    public ResponseEntity<List<FieldResponse>> getAllFields(
            @RequestParam(required = false) Boolean active
    ) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(fieldService.getAllForFarmer(farmerId, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldResponse> getField(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FieldResponse> create(@Valid @RequestBody FieldRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        FieldResponse created = fieldService.create(request, farmerId);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FieldResponse> update(@PathVariable Long id, @Valid @RequestBody FieldRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        boolean deleted = fieldService.delete(id, farmerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<List<FieldResponse>> createBatch(@Valid @RequestBody List<FieldRequest> requests) {
        Long farmerId = getAuthenticatedFarmerId();
        List<FieldResponse> created = fieldService.createBatch(requests, farmerId);
        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        Long farmerId = getAuthenticatedFarmerId();
        int deletedCount = fieldService.deleteBatch(ids, farmerId);
        return deletedCount > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/batch/hard")
    public ResponseEntity<Void> hardDeleteBatch(@RequestBody HardDeleteRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        fieldService.hardDeleteBatch(request.ids(), farmerId, request.cascade());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/batch/active")
    public ResponseEntity<Void> updateActiveBatch(@RequestBody BatchActiveRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        fieldService.updateActiveBatch(request.ids(), farmerId, request.active());
        return ResponseEntity.noContent().build();
    }
}
