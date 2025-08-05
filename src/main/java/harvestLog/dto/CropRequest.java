package harvestLog.dto;

import harvestLog.model.Category;
import harvestLog.model.MeasureUnit;

public record CropRequest(
        String name,
        Long measureUnitId,
        String categoryName

) {
}
