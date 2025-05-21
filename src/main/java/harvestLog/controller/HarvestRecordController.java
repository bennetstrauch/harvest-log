package harvestLog.controller;

import harvestLog.dto.HarvestRecordRequest;
import harvestLog.dto.HarvestRecordResponse;
import harvestLog.service.IHarvestRecordService;
import harvestLog.service.impl.HarvestRecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/harvest-record")
public class HarvestRecordController {

    private final IHarvestRecordService recordService;
    public HarvestRecordController(IHarvestRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public List<HarvestRecordResponse> getAllHarvestRecords() {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.getForFarmer(farmerId);
    }

    @GetMapping("/filtered")
    public List<HarvestRecordResponse> getFiltered(
            @RequestParam(required = false) List<Long> fieldIds,
            @RequestParam(required = false) List<Long> cropIds,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.getFilteredRecords(farmerId, fieldIds, cropIds, startDate, endDate);
    }

    @PostMapping
    public HarvestRecordResponse create(@Valid @RequestBody HarvestRecordRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.create(request, farmerId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HarvestRecordResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody HarvestRecordRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        boolean deletionSuccessful = recordService.delete(id, farmerId);
        return deletionSuccessful
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}

