package harvestLog.dto;

import java.util.List;

public record TrimToFreeRequest(
        List<Long> keepCropIds,
        List<Long> keepFieldIds,
        List<Long> keepMeasureUnitIds
) {}
