package harvestLog.service;

import harvestLog.exception.PlanLimitExceededException;
import harvestLog.model.Farmer;
import harvestLog.model.PlanType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlanService {

    public PlanType getEffectivePlan(Farmer farmer) {
        if (farmer.getPlanType() == PlanType.FARM) {
            return PlanType.FARM;
        }
        if (farmer.getTrialEndsAt() != null && farmer.getTrialEndsAt().isAfter(LocalDateTime.now())) {
            return PlanType.FARM;
        }
        return PlanType.FREE;
    }

    public void enforceLimit(Farmer farmer, long current, int adding, int max, String entityLabel) {
        if (getEffectivePlan(farmer) != PlanType.FREE) return;
        if (current + adding > max) {
            String msg = adding == 1
                    ? "Free plan allows max " + max + " active " + entityLabel + "."
                    : "Free plan allows max " + max + " active " + entityLabel
                            + ". You have " + current + " and are trying to add " + adding + ".";
            throw new PlanLimitExceededException(msg);
        }
    }
}
