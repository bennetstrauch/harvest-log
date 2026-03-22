package harvestLog.controller;

import harvestLog.model.Farmer;
import harvestLog.model.PlanType;
import harvestLog.plan.PlanLimits;
import harvestLog.service.PlanService;
import harvestLog.service.impl.FarmerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final FarmerService farmerService;
    private final PlanService planService;

    public PlanController(FarmerService farmerService, PlanService planService) {
        this.farmerService = farmerService;
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPlanInfo() {
        Long farmerId = getAuthenticatedFarmerId();
        Farmer farmer = farmerService.findById(farmerId);
        PlanType effective = planService.getEffectivePlan(farmer);

        return ResponseEntity.ok(Map.of(
                "plan", effective.name(),
                "trialEndsAt", farmer.getTrialEndsAt() != null ? farmer.getTrialEndsAt().toString() : "",
                "trialActive", effective == PlanType.FARM && farmer.getPlanType() == PlanType.FREE,
                "limits", Map.of(
                        "maxCrops", PlanLimits.FREE_MAX_CROPS,
                        "maxFields", PlanLimits.FREE_MAX_FIELDS,
                        "maxMeasureUnits", PlanLimits.FREE_MAX_MEASURE_UNITS
                )
        ));
    }
}
