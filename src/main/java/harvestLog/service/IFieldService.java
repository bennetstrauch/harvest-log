package harvestLog.service;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;

import java.util.List;
import java.util.Optional;

public interface IFieldService {

    List<FieldResponse> getAllForFarmer(Long farmerId);
    List<FieldResponse> getActiveForFarmer(Long farmerId);
    List<FieldResponse> getInactiveForFarmer(Long farmerId);
    List<FieldResponse> getAllForFarmer(Long farmerId, Boolean active);

    Optional<FieldResponse> getById(Long id, Long farmerId);

    FieldResponse create(FieldRequest request, Long farmerId);

    List<FieldResponse> createBatch(List<FieldRequest> requests, Long farmerId);

    Optional<FieldResponse> update(Long id, FieldRequest request, Long farmerId);

    boolean delete(Long id, Long farmerId);

    int deleteBatch(List<Long> ids, Long farmerId);
    void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade);
    void updateActiveBatch(List<Long> ids, Long farmerId, boolean active);
}
