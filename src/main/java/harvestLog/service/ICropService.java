package harvestLog.service;


import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.model.Category;
import harvestLog.model.Crop;

import java.util.List;

public interface ICropService {
    CropResponse addCrop(CropRequest crop);
    List<CropResponse> getAllCrops();
    List<CropResponse> searchByCategory(Category category);
    List<CropResponse> findByNameContains(String s);
    CropResponse getCropById(Long id);
    CropResponse updateCrop(Long id, CropRequest crop);
    void deleteCrop(Long id);

    List<HarvestSummaryResponse> getHarvestsByCrop(Long id);
}
