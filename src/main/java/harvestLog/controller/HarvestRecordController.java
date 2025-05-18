package harvestLog.controller;

import harvestLog.model.Farmer;
import harvestLog.model.HarvestRecord;
import harvestLog.service.HarvestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/harvest-record")
public class HarvestRecordController {

    @Autowired private HarvestRecordService recordService;

    @GetMapping
    public List<HarvestRecord> getAllHarvestRecords() {
        Long farmerId = getAuthenticatedFarmerId();

        return recordService.getForFarmer(farmerId);
    }

    @GetMapping("/filtered")
    public List<HarvestRecord> getFiltered(
            @RequestParam(required = false) List<Long> fieldIds,
            @RequestParam(required = false) List<Long> cropIds,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.getFilteredRecords(farmerId, fieldIds, cropIds, startDate, endDate);
    }

    @PostMapping
    public HarvestRecord create(@RequestBody HarvestRecord record) {
        Long farmerId = getAuthenticatedFarmerId();
        record.setFarmer(new Farmer(farmerId)); // Set farmer reference
        return recordService.create(record);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HarvestRecord> update(@PathVariable Long id, @RequestBody HarvestRecord record) {
        Long farmerId = getAuthenticatedFarmerId();
        record.setFarmer(new Farmer(farmerId)); // Ensure farmer consistency
        return recordService.update(id, record)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        ##can be aop?
        Long farmerId = getAuthenticatedFarmerId();
        boolean deletionSuccessful = recordService.delete(id, farmerId);


            return deletionSuccessful
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
    }
}
