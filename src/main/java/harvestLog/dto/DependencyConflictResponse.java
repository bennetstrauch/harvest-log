package harvestLog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DependencyConflictResponse(
        String message,
        String code,
        List<EntitySummary> affectedCrops,
        int affectedHarvestRecordCount,
        LocalDateTime timestamp
) {
    public record EntitySummary(Long id, String name) {}
}
