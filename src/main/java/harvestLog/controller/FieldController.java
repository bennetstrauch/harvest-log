package harvestLog.controller;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import harvestLog.service.FieldService;
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
    public List<FieldResponse> getAllFields() {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.getAllForFarmer(farmerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldResponse> getField(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FieldResponse create(@Valid @RequestBody FieldRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.create(request, farmerId);
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
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}package harvestLog.controller;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import harvestLog.service.FieldService;
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
    public List<FieldResponse> getAllFields() {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.getAllForFarmer(farmerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldResponse> getField(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FieldResponse create(@Valid @RequestBody FieldRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return fieldService.create(request, farmerId);
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
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}