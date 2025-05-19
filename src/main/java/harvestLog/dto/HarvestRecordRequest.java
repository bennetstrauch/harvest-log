package harvestLog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public record HarvestRecordRequest(
        @NotNull(message = "Date is required")
        LocalDate date,
        @NotNull(message = "Crop ID is required")
        @Positive(message = "Crop ID must be positive")
        Long cropId,
        @NotNull(message = "Field IDs are required")
        List<@Positive(message = "Field ID must be positive") Long> fieldIds,
        @NotNull(message = "Harvested quantity is required")
        @Positive(message = "Harvested quantity must be positive")
        Double harvestedQuantity
) {}