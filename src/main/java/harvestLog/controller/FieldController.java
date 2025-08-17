package harvestLog.controller;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import harvestLog.service.impl.FieldService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @GetMapping
    public ResponseEntity<List<FieldResponse>> getAllFields() {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(fieldService.getAllForFarmer(farmerId));
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

}
