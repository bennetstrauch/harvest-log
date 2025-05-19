package harvestLog.dto;

import harvestLog.model.MeasureUnit;

public record CropResponse(
        Long id,
        String name,
        MeasureUnit measureUnit
) {
}
