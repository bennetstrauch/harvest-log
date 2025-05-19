package harvestLog.service;


import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.model.Crop;

import java.util.List;

public interface ICropService {
    CropResponse addCrop(CropRequest crop);
    List<CropResponse> getAllCrops();
    CropResponse getCropById(Long id);
    CropResponse updateCrop(Long id, CropRequest crop);
    void deleteCrop(Long id);
}
