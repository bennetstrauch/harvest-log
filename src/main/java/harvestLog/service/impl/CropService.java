package harvestLog.service.impl;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.model.Category;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.service.ICropService;
import harvestLog.service.ICategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropService implements ICropService {

    private final CropRepository cropRepo;
    private final FarmerRepository farmerRepo;
    private final ICategoryService categoryService;

    public CropService(CropRepository cropRepo, FarmerRepository farmerRepo, ICategoryService categoryService) {
        this.cropRepo = cropRepo;
        this.farmerRepo = farmerRepo;
        this.categoryService = categoryService;
    }

    @Override
    public List<CropResponse> getAll(Long farmerId) {
        return cropRepo.findByFarmerId(farmerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CropResponse> getById(Long id, Long farmerId) {
        return cropRepo.findById(id)
                .filter(crop -> crop.getFarmer().getId().equals(farmerId))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public CropResponse create(CropRequest request, Long farmerId) {
        Crop crop = toEntity(request, farmerId);
        Crop saved = cropRepo.save(crop);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<CropResponse> createBatch(List<CropRequest> requests, Long farmerId) {
        List<Crop> crops = requests.stream()
                .map(req -> toEntity(req, farmerId))
                .collect(Collectors.toList());
        List<Crop> saved = cropRepo.saveAll(crops);
        return saved.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public Optional<CropResponse> update(Long id, CropRequest request, Long farmerId) {
        return cropRepo.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmerId))
                .map(c -> {
                    c.setName(request.name());
                    if (request.categoryName() != null) {
                        Category cat = categoryService.getOrCreateActiveByName(request.categoryName(), farmerId);
                        c.setCategory(cat);
                    }
                    return toResponse(cropRepo.save(c));
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long farmerId) {
        return cropRepo.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmerId))
                .map(c -> {
                    cropRepo.delete(c);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId, Long farmerId) {
        // TODO implement via harvestRepository if needed
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CropResponse> searchByCategoryName(String categoryName, Long farmerId) {
        return cropRepo.findByCategoryNameIgnoreCaseAndFarmerId(categoryName, farmerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<List<CropResponse>> findByNameContains(String substring, Long farmerId) {
        List<CropResponse> results = cropRepo.findByNameContainingIgnoreCaseAndFarmerId(substring, farmerId).stream()
                .map(this::toResponse)
                .toList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results);
    }

    // ============================
    // Mapping helpers
    // ============================
    private Crop toEntity(CropRequest request, Long farmerId) {
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        Crop crop = new Crop();
        crop.setName(request.name());
        crop.setFarmer(farmer);

        if (request.categoryName() != null) {
            Category category = categoryService.getOrCreateActiveByName(request.categoryName(), farmerId);
            crop.setCategory(category);
        }
        return crop;
    }

    private CropResponse toResponse(Crop crop) {
        Long measureUnitId = crop.getMeasureUnit() != null ? crop.getMeasureUnit().getId() : null;
        Long categoryId = crop.getCategory() != null ? crop.getCategory().getId() : null;
        String categoryName = crop.getCategory() != null ? crop.getCategory().getName() : null;

        return new CropResponse(
                crop.getId(),
                crop.getName(),
                measureUnitId,
                categoryId,
                categoryName
        );
    }

}
