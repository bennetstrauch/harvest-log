package harvestLog.dto;

import jakarta.validation.constraints.NotBlank;

public record FieldRequest(
        @NotBlank(message = "Name is required")
        String name
) {}

