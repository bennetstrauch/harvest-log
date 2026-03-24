package harvestLog.dto;

import harvestLog.model.PlanType;
import jakarta.validation.constraints.NotNull;

public record AdminPlanUpdateRequest(
        @NotNull(message = "planType is required")
        PlanType planType
) {}
