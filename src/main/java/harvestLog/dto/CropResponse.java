package harvestLog.dto;

public record CropResponse(
        Long id,
        String name,
        Long measureUnitId,
        String categoryName
) {
}
