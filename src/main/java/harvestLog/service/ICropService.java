package harvestLog.service;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface ICropService {
    List<CropResponse> getAll(Long farmerId);
    Optional<CropResponse> getById(Long id, Long farmerId);
    CropResponse create(CropRequest request, Long farmerId);
    List<CropResponse> createBatch(List<CropRequest> requests, Long farmerId);
    Optional<CropResponse> update(Long id, CropRequest request, Long farmerId);
    boolean delete(Long id, Long farmerId);

    List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId, Long farmerId);
    List<CropResponse> searchByCategoryName(String categoryName, Long farmerId);
    Optional<List<CropResponse>> findByNameContains(String substring, Long farmerId);
}
