package harvestLog.service;


import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface ICropService {
    CropResponse addCrop(CropRequest crop);
    List<CropResponse> addCrops(List<CropRequest> cropRequests);
    List<CropResponse> getAllCrops();
    List<CropResponse> searchByCategoryName(String categoryName);
    Optional<List<CropResponse>> findByNameContains(String s);
    CropResponse getCropById(Long id);
    CropResponse updateCrop(Long id, CropRequest crop);
    void deleteCrop(Long id);
    CropResponse getCropByName(String cropName);

    List<HarvestSummaryResponse> getHarvestsByCrop(Long id);
}
