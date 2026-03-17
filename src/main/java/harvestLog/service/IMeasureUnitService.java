package harvestLog.service;

import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.model.MeasureUnit;

import java.util.List;
import java.util.Optional;

public interface IMeasureUnitService {
    List<MeasureUnitResponse> getAllForFarmerId(Long farmerId);
    List<MeasureUnitResponse> getActiveForFarmerId(Long farmerId);
    List<MeasureUnitResponse> getInactiveForFarmerId(Long farmerId);
    List<MeasureUnitResponse> getAllForFarmerId(Long farmerId, Boolean active);

    Optional<MeasureUnitResponse> getById(Long id, Long farmerId);

    MeasureUnitResponse create(MeasureUnitRequest request, Long farmerId);
    List<MeasureUnitResponse> createBatch(List<MeasureUnitRequest> requests, Long farmerId);
    Optional<MeasureUnitResponse> update(Long id, MeasureUnitRequest request, Long farmerId);

    boolean delete(Long id, Long farmerId);
    int deleteBatch(List<Long> ids, Long farmerId);
    void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade);
    void updateActiveBatch(List<Long> ids, Long farmerId, boolean active);

}
