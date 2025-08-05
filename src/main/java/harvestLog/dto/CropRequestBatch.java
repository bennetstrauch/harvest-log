package harvestLog.dto;

import java.util.List;

public record CropRequestBatch(
        Long measureUnitId,
        List<String> cropNames
) { }
