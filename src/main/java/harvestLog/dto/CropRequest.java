package harvestLog.dto;

import harvestLog.model.MeasureUnit;

public record CropRequest(
        String name,
        MeasureUnit measureUnit
) {
}
