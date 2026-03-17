package harvestLog.dto;

public record CropRequest(
        String name,
        Long measureUnitId,
        Long categoryId,
        Boolean active
) {}
