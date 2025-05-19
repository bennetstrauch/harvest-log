package harvestLog.service.impl;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.exception.EntityNotFoundException;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.service.ICropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropService implements ICropService {
    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    private Farmer getCurrentFarmer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return farmerRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Farmer not found"));
    }

    @Override
    public CropResponse addCrop(CropRequest dto) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = new Crop();
        crop.setName(dto.name());
        crop.setMeasureUnit(dto.measureUnit());
        crop.setFarmer(farmer);
        Crop savedCrop = cropRepository.save(crop);
        return new CropResponse(savedCrop.getId(), savedCrop.getName(), savedCrop.getMeasureUnit());
    }

    @Override
    public List<CropResponse> getAllCrops() {
        Farmer farmer = getCurrentFarmer();
        return cropRepository.findByFarmer(farmer).stream().map(crop ->
                new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit())
        ).collect(Collectors.toList());
    }

    @Override
    public CropResponse getCropById(Long id) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id).filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
        return new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit());
    }

    @Override
    public CropResponse updateCrop(Long id, CropRequest dto) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id).filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
        crop.setName(dto.name());
        crop.setMeasureUnit(dto.measureUnit());
        Crop updated = cropRepository.save(crop);
        return new CropResponse(updated.getId(), updated.getName(), updated.getMeasureUnit());
    }

    @Override
    public void deleteCrop(Long id) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        cropRepository.delete(crop);
    }
}
