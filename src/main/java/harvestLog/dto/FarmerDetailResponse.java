package harvestLog.dto;

import java.util.List;

public record FarmerDetailResponse(
        Long id,
        String name,
        String email,
        List<Long> harvestRecordIds,
        List<Long> cropIds,
        List<Long> fieldIds

) {
}
