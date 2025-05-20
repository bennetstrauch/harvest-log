package harvestLog.dto;

import harvestLog.model.Category;
import harvestLog.model.MeasureUnit;

public record CropResponse(
        Long id,
        String name,
        MeasureUnit measureUnit,
        Category category
) {
}
