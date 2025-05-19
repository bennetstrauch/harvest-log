package harvestLog.dto;

import java.time.LocalDate;
import java.util.List;

public record HarvestRecordResponse(
        Long id,
        LocalDate date,
        Long cropId,
        List<Long> fieldIds,
        Double harvestedQuantity,
        Long farmerId
) {}