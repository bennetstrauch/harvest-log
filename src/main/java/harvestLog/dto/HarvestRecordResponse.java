package harvestLog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HarvestRecordResponse(
        Long id,
        LocalDate date,
        LocalDateTime createdAt,
        Long cropId,
        List<Long> fieldIds,
        Double harvestedQuantity,
        Long farmerId,
        boolean archived,
        String archivedCropName,
        String archivedFieldNames,
        String archivedMeasureUnitName,
        String archivedCategoryName
) {}