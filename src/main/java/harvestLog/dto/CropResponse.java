package harvestLog.dto;

public record CropResponse(
        Long id,
        String name,
        Long measureUnitId,
        Long categoryId,
        boolean active,

        String categoryName,
        boolean categoryResolved,
        String categorySuggestion
) {}
