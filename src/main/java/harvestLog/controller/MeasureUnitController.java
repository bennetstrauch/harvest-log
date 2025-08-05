package harvestLog.controller;

import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.service.impl.MeasureUnitService;
import harvestLog.model.MeasureUnit;
import harvestLog.model.Farmer;
import harvestLog.repository.FarmerRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/measure-units")
public class MeasureUnitController {

    private final MeasureUnitService measureUnitService;
    private final FarmerRepository farmerRepository;

    public MeasureUnitController(MeasureUnitService measureUnitService, FarmerRepository farmerRepository) {
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
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new MeasureUnitResponse(unit.getId(), unit.getName(), unit.getAbbreviation()));
    }

    @PostMapping
    public ResponseEntity<MeasureUnitResponse> create(@Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        Farmer farmer = farmerRepository.findById(farmerId).orElseThrow(); // Could throw a custom exception
        MeasureUnit unit = new MeasureUnit(null, farmer, request.name(), request.abbreviation());
        MeasureUnit saved = measureUnitService.save(unit);
        return ResponseEntity.ok(new MeasureUnitResponse(saved.getId(), saved.getName(), saved.getAbbreviation()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeasureUnitResponse> update(@PathVariable Long id, @Valid @RequestBody MeasureUnitRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnit existing = measureUnitService.getById(id);
        if (!existing.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.notFound().build();
        }
        existing.setName(request.name());
        existing.setAbbreviation(request.abbreviation());
        MeasureUnit updated = measureUnitService.save(existing);
        return ResponseEntity.ok(new MeasureUnitResponse(updated.getId(), updated.getName(), updated.getAbbreviation()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        MeasureUnit existing = measureUnitService.getById(id);
        if (!existing.getFarmer().getId().equals(farmerId)) {
            return ResponseEntity.notFound().build();
        }
        measureUnitService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
