package harvestLog.controller;

import harvestLog.dto.TrimToFreeRequest;
import harvestLog.model.Farmer;
import harvestLog.model.PlanType;
import harvestLog.plan.PlanLimits;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.FieldRepository;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.service.ICropService;
import harvestLog.service.IFieldService;
import harvestLog.service.PlanService;
import harvestLog.service.impl.FarmerService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final FarmerService farmerService;
    private final FarmerRepository farmerRepository;
    private final PlanService planService;
    private final CropRepository cropRepository;
    private final FieldRepository fieldRepository;
    private final MeasureUnitRepository measureUnitRepository;
    private final ICropService cropService;
    private final IFieldService fieldService;

    public PlanController(FarmerService farmerService, FarmerRepository farmerRepository,
                          PlanService planService, CropRepository cropRepository,
                          FieldRepository fieldRepository, MeasureUnitRepository measureUnitRepository,
                          ICropService cropService, IFieldService fieldService) {
        this.farmerService = farmerService;
        this.farmerRepository = farmerRepository;
        this.planService = planService;
        this.cropRepository = cropRepository;
        this.fieldRepository = fieldRepository;
        this.measureUnitRepository = measureUnitRepository;
        this.cropService = cropService;
        this.fieldService = fieldService;
    }

    @GetMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> getPlanInfo() {
        Long farmerId = getAuthenticatedFarmerId();
        Farmer farmer = farmerService.findById(farmerId);
        PlanType effective = planService.getEffectivePlan(farmer);

        long cropCount = cropRepository.findByFarmerId(farmerId).size();
        long fieldCount = fieldRepository.findByFarmerId(farmerId).size();
        long muCount = measureUnitRepository.findAllByFarmer_Id(farmerId).size();

        // Lazily initialize grace period if FREE plan with overage
        LocalDateTime gracePeriodEndsAt = null;
        if (effective == PlanType.FREE) {
            boolean hasOverage = cropCount > PlanLimits.FREE_MAX_CROPS
                    || fieldCount > PlanLimits.FREE_MAX_FIELDS
                    || muCount > PlanLimits.FREE_MAX_MEASURE_UNITS;
            if (hasOverage) {
                if (farmer.getGracePeriodStartedAt() == null) {
                    farmer.setGracePeriodStartedAt(LocalDateTime.now());
                    farmerRepository.save(farmer);
                }
                gracePeriodEndsAt = farmer.getGracePeriodStartedAt().plusDays(5);
            } else if (farmer.getGracePeriodStartedAt() != null) {
                // Overage resolved externally — clear the grace period
                farmer.setGracePeriodStartedAt(null);
                farmerRepository.save(farmer);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("plan", effective.name());
        response.put("trialEndsAt", farmer.getTrialEndsAt() != null ? farmer.getTrialEndsAt().toString() : "");
        response.put("trialActive", effective == PlanType.FARM && farmer.getPlanType() == PlanType.FREE);
        response.put("limits", Map.of(
                "maxCrops", PlanLimits.FREE_MAX_CROPS,
                "maxFields", PlanLimits.FREE_MAX_FIELDS,
                "maxMeasureUnits", PlanLimits.FREE_MAX_MEASURE_UNITS
        ));
        response.put("currentCounts", Map.of(
                "crops", cropCount,
                "fields", fieldCount,
                "measureUnits", muCount
        ));
        response.put("gracePeriodEndsAt", gracePeriodEndsAt != null ? gracePeriodEndsAt.toString() : null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trim-to-free")
    @Transactional
    public ResponseEntity<Void> trimToFree(@RequestBody TrimToFreeRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        Farmer farmer = farmerService.findById(farmerId);

        List<Long> keepCropIds = request.keepCropIds() != null ? request.keepCropIds() : List.of();
        List<Long> keepFieldIds = request.keepFieldIds() != null ? request.keepFieldIds() : List.of();
        List<Long> keepMuIds = request.keepMeasureUnitIds() != null ? request.keepMeasureUnitIds() : List.of();

        // Validate sizes
        if (keepCropIds.size() > PlanLimits.FREE_MAX_CROPS) {
            return ResponseEntity.badRequest().build();
        }
        if (keepFieldIds.size() > PlanLimits.FREE_MAX_FIELDS) {
            return ResponseEntity.badRequest().build();
        }
        if (keepMuIds.size() > PlanLimits.FREE_MAX_MEASURE_UNITS) {
            return ResponseEntity.badRequest().build();
        }

        // 1. Delete excess crops (cascade archives harvest records)
        List<Long> allCropIds = cropRepository.findByFarmerId(farmerId)
                .stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Long> excessCropIds = allCropIds.stream()
                .filter(id -> !keepCropIds.contains(id))
                .collect(Collectors.toList());
        if (!excessCropIds.isEmpty()) {
            cropService.hardDeleteBatch(excessCropIds, farmerId, true);
        }

        // 2. Delete excess fields (cascade archives harvest record field references)
        List<Long> allFieldIds = fieldRepository.findByFarmerId(farmerId)
                .stream().map(f -> f.getId()).collect(Collectors.toList());
        List<Long> excessFieldIds = allFieldIds.stream()
                .filter(id -> !keepFieldIds.contains(id))
                .collect(Collectors.toList());
        if (!excessFieldIds.isEmpty()) {
            fieldService.hardDeleteBatch(excessFieldIds, farmerId, true);
        }

        // 3. Delete excess measure units (orphaned after crop deletion)
        List<Long> allMuIds = measureUnitRepository.findAllByFarmer_Id(farmerId)
                .stream().map(m -> m.getId()).collect(Collectors.toList());
        List<Long> excessMuIds = allMuIds.stream()
                .filter(id -> !keepMuIds.contains(id))
                .collect(Collectors.toList());
        if (!excessMuIds.isEmpty()) {
            measureUnitRepository.deleteByIdInAndFarmerId(excessMuIds, farmerId);
        }

        // 4. Clear grace period
        farmer.setGracePeriodStartedAt(null);
        farmerRepository.save(farmer);

        return ResponseEntity.ok().build();
    }
}
