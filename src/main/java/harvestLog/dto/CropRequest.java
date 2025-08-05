package harvestLog.dto;

public record CropRequest(
        String name,
        Long measureUnitId,
        String categoryName

) {
}
