package harvestLog.dto;

import jakarta.validation.constraints.NotBlank;

public record MeasureUnitRequest(
        @NotBlank(message = "Name is required")
        String name,

        String abbreviation
) {}
