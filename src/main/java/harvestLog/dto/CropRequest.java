package harvestLog.dto;

import harvestLog.model.Category;
import harvestLog.model.MeasureUnit;

public record CropRequest(
        String name,
        MeasureUnit measureUnit,
        Category category

) {
}
