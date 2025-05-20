package harvestLog.dto;

import java.time.LocalDate;
import java.util.List;

public record HarvestSummaryResponse(
        Long id,
        LocalDate date,
        double harvestedQuantity,
        List<Long> fieldIds
) {
}
