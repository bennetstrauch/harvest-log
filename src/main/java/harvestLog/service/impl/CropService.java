package harvestLog.service.impl;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.exception.EntityNotFoundException;
import harvestLog.model.*;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.HarvestRecordRepository;
import harvestLog.service.ICropService;
import harvestLog.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropService implements ICropService {
    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private HarvestRecordRepository harvestRecordRepository;

    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private MeasureUnitService measureUnitService;

//    ##change to same as field getAutentcatedFarmerId in Controller
    private Farmer getCurrentFarmer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return farmerRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Farmer not found"));
    }

    @Override
    public List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId) {
        List<HarvestRecord> records = harvestRecordRepository.findByCrop_Id(cropId);
        return records.stream().map(record ->
                new HarvestSummaryResponse(
                        record.getId(),
                        record.getDate(),
                        record.getHarvestedQuantity(),
                        record.getFields().stream().map(Field::getId).toList())
        ).toList();
    }

    @Override
    public CropResponse addCrop(CropRequest dto) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = new Crop();
        MeasureUnit measureUnit= measureUnitService.getById(dto.measureUnitId());
        crop.setName(dto.name());
        crop.setMeasureUnit(measureUnit);
        crop.setFarmer(farmer);

        // Resolve category by name using helper service
        Category category = categoryService.findByNameOrCreate(dto.categoryName());
        crop.setCategory(category);

        Crop savedCrop = cropRepository.save(crop);
        return new CropResponse(savedCrop.getId(), savedCrop.getName(), savedCrop.getMeasureUnit().getId(), savedCrop.getCategory().getName());
    }

    @Override
    public List<CropResponse> addCrops(List<CropRequest> cropRequests) {
        return cropRequests.stream()
                .map(this::addCrop)
                .collect(Collectors.toList());
    }

    @Override
    public List<CropResponse> getAllCrops() {
        Farmer farmer = getCurrentFarmer();
        return cropRepository.findByFarmer(farmer).stream().map(crop ->
                new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit().getId(), crop.getCategory().getName())
        ).collect(Collectors.toList());
    }

    @Override
    public List<CropResponse> searchByCategoryName(String categoryName) {
        return cropRepository.findCropsByCategoryName(categoryName).stream().map(crop ->
                new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit().getId(), crop.getCategory().getName())
        ).collect(Collectors.toList());
    }

    @Override
    public Optional<List<CropResponse>> findByNameContains(String s) {
        return cropRepository.findByNameContainingIgnoreCase(s).map(crops -> crops.stream().map(
                crop -> new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit().getId(), crop.getCategory().getName())
        ).collect(Collectors.toList()));
    }

    @Override
    public CropResponse getCropById(Long id) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id).filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
        return new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit().getId(), crop.getCategory().getName());
    }

    @Override
    public CropResponse updateCrop(Long id, CropRequest dto) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id).filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
        MeasureUnit measureUnit = measureUnitService.getById(dto.measureUnitId());

        crop.setName(dto.name());
        crop.setMeasureUnit(measureUnit);

        Category category = categoryService.findByNameOrCreate(dto.categoryName());
        crop.setCategory(category);

        Crop updated = cropRepository.save(crop);
        return new CropResponse(updated.getId(), updated.getName(), updated.getMeasureUnit().getId(), updated.getCategory().getName());
    }

    @Override
    public void deleteCrop(Long id) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        cropRepository.delete(crop);
    }

    @Override
    public CropResponse getCropByName(String cropName) {
        Farmer farmer = getCurrentFarmer();
        Crop crop = cropRepository.findCropByNameContainingIgnoreCase(cropName).filter(c -> c.getFarmer().getId().equals(farmer.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
        return new CropResponse(crop.getId(), crop.getName(), crop.getMeasureUnit().getId(), crop.getCategory().getName());
    }
}
