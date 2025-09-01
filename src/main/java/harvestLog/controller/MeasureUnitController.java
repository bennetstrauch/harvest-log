package harvestLog.controller;

import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.service.IMeasureUnitService;
import harvestLog.model.MeasureUnit;
import harvestLog.model.Farmer;
import harvestLog.repository.FarmerRepository;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/measure-units")
public class MeasureUnitController {

    private final IMeasureUnitService measureUnitService;
    private final FarmerRepository farmerRepository;

    public MeasureUnitController(IMeasureUnitService measureUnitService,
                                 FarmerRepository farmerRepository) {
        this.measureUnitService = measureUnitService;
        this.farmerRepository = farmerRepository;
    }

    @GetMapping
    public List<MeasureUnitResponse> getAll() {
        Long farmerId = getAuthenticatedFarmerId();
        return measureUnitService.getAllForFarmerId(farmerId).stream()
                .map(unit -> new MeasureUnitResponse(unit.getId(), unit.getName(), unit.getAbbreviation()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeasureUnitResponse> getById(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnit unit = measureUnitService.getById(id);

        if (!unit.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.status(403).build(); // ✅ Forbidden instead of 404
        }

        return ResponseEntity.ok(new MeasureUnitResponse(unit.getId(), unit.getName(), unit.getAbbreviation()));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        Farmer farmer = farmerRepository.findById(farmerId).orElseThrow();

        try {
            MeasureUnit unit = new MeasureUnit(farmer, request.name(), request.abbreviation());
            MeasureUnit saved = measureUnitService.save(unit);
            return ResponseEntity.ok(new MeasureUnitResponse(saved.getId(), saved.getName(), saved.getAbbreviation()));
        } catch (DataIntegrityViolationException e) {
            // ✅ handles unique constraint (farmer_id + name)
            return ResponseEntity.badRequest().body("Measure unit with this name already exists.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnit existing = measureUnitService.getById(id);

        if (!existing.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.status(403).build();
        }

        existing.setName(request.name());
        existing.setAbbreviation(request.abbreviation());

        try {
            MeasureUnit updated = measureUnitService.save(existing);
            return ResponseEntity.ok(new MeasureUnitResponse(updated.getId(), updated.getName(), updated.getAbbreviation()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Measure unit with this name already exists.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnit existing = measureUnitService.getById(id);

        if (!existing.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.status(403).build();
        }

        // ✅ use soft delete
        existing.setActive(false);
        measureUnitService.save(existing);

        return ResponseEntity.ok().build();
    }
}
