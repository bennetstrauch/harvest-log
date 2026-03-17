package harvestLog.service;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface ICropService {
    List<CropResponse> getAll(Long farmerId);
    List<CropResponse> getAllActive(Long farmerId);
    List<CropResponse> getAllInactive(Long farmerId);
    List<CropResponse> getAll(Long farmerId, Boolean active);
    Optional<CropResponse> getById(Long id, Long farmerId);
    CropResponse create(CropRequest request, Long farmerId);
    List<CropResponse> createBatch(List<CropRequest> requests, Long farmerId);
    Optional<CropResponse> update(Long id, CropRequest request, Long farmerId);
    boolean delete(Long id, Long farmerId);
    int deleteBatch(List<Long> ids, Long farmerId);
    void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade);
    void updateActiveBatch(List<Long> ids, Long farmerId, boolean active);

//    List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId, Long farmerId);
//    List<CropResponse> searchByCategoryName(String categoryName, Long farmerId);
//    Optional<List<CropResponse>> findByNameContains(String substring, Long farmerId);
}
