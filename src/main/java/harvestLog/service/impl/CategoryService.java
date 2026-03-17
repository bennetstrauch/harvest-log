package harvestLog.service.impl;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.dto.DependencyConflictResponse;
import harvestLog.exception.AlreadyExistsException;
import harvestLog.exception.DependencyConflictException;
import harvestLog.model.Category;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.model.HarvestRecord;
import harvestLog.repository.CategoryRepository;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.HarvestRecordRepository;
import harvestLog.service.ICategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository repository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final HarvestRecordRepository harvestRecordRepository;

    public CategoryService(CategoryRepository repository, FarmerRepository farmerRepository,
                           CropRepository cropRepository, HarvestRecordRepository harvestRecordRepository) {
        this.repository = repository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.harvestRecordRepository = harvestRecordRepository;
    }

    @Override
    public List<CategoryResponse> getAllForFarmerId(Long farmerId) {
        return repository.findByFarmerId(farmerId, null).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveForFarmerId(Long farmerId) {
        return repository.findByFarmerIdAndActive(farmerId, true).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getInactiveForFarmerId(Long farmerId) {
        return repository.findByFarmerIdAndActive(farmerId, false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllForFarmerId(Long farmerId, Boolean active) {
        if (active == null) {
            return getAllForFarmerId(farmerId);
        }
        return active ? getActiveForFarmerId(farmerId) : getInactiveForFarmerId(farmerId);
    }

    @Override
    public Optional<CategoryResponse> getById(Long id, Long farmerId) {
        return repository.findById(id)
                .filter(category -> category.getFarmer().getId().equals(farmerId))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request, Long farmerId) {
        try {
            Category category = toEntity(request, farmerId);
            Category saved = repository.save(category);
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            return handleDuplicateCategory(request, farmerId);
        }
    }

    private CategoryResponse handleDuplicateCategory(CategoryRequest request, Long farmerId) {
        Optional<Category> existing = repository.findByNameIgnoreCaseAndFarmerId(request.name().toUpperCase(), farmerId);

        if (existing.isPresent() && !existing.get().isActive()) {
            Category category = existing.get();
            category.setActive(true);
            return toResponse(repository.save(category));
        }

        throw new AlreadyExistsException("Active category with name '" + request.name() + "' already exists");
    }

    @Override
    @Transactional
    public List<CategoryResponse> createBatch(List<CategoryRequest> requests, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        List<Category> categories = requests.stream()
                .map(request -> {
                    Category category = new Category();
                    category.setName(request.name().toUpperCase());
                    category.setFarmer(farmer);
                    category.setActive(request.active() != null ? request.active() : true);
                    return category;
                })
                .collect(Collectors.toList());

        List<Category> saved = repository.saveAll(categories);
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<CategoryResponse> update(Long id, CategoryRequest request, Long farmerId) {
        return repository.findById(id)
                .filter(category -> category.getFarmer().getId().equals(farmerId))
                .map(category -> {
                    if (request.name() != null) {
                        category.setName(request.name().toUpperCase());
                    }
                    if (request.active() != null) {
                        category.setActive(request.active());
                    }
                    return toResponse(repository.save(category));
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long farmerId) {
        return repository.softDeleteByIdInAndFarmerId(List.of(id), farmerId) > 0;
    }

    @Override
    @Transactional
    public int deleteBatch(List<Long> ids, Long farmerId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return repository.softDeleteByIdInAndFarmerId(ids, farmerId);
    }

    @Override
    @Transactional
    public void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade) {
        if (ids == null || ids.isEmpty()) return;

        List<Crop> affectedCrops = cropRepository.findByCategory_IdInAndFarmerId(ids, farmerId);

        if (!affectedCrops.isEmpty() && !cascade) {
            List<Long> cropIds = affectedCrops.stream().map(Crop::getId).collect(Collectors.toList());
            int hrCount = harvestRecordRepository.countByFarmerIdAndCrop_IdIn(farmerId, cropIds);
            List<DependencyConflictResponse.EntitySummary> cropSummaries = affectedCrops.stream()
                    .map(c -> new DependencyConflictResponse.EntitySummary(c.getId(), c.getName()))
                    .collect(Collectors.toList());
            throw new DependencyConflictException(new DependencyConflictResponse(
                    "This category is used by crops which may have harvest records",
                    "DEPENDENCY_CONFLICT",
                    cropSummaries,
                    hrCount,
                    LocalDateTime.now()
            ));
        }

        if (!affectedCrops.isEmpty()) {
            List<Long> cropIds = affectedCrops.stream().map(Crop::getId).collect(Collectors.toList());
            // Archive harvest records for these crops
            Map<Long, String> cropNameMap = affectedCrops.stream()
                    .collect(Collectors.toMap(Crop::getId, Crop::getName));
            Map<Long, String> cropMuMap = affectedCrops.stream()
                    .filter(c -> c.getMeasureUnit() != null)
                    .collect(Collectors.toMap(Crop::getId, c -> {
                        var mu = c.getMeasureUnit();
                        return (mu.getAbbreviation() != null && !mu.getAbbreviation().isBlank())
                                ? mu.getAbbreviation() : mu.getName();
                    }));
            List<HarvestRecord> affected = harvestRecordRepository.findByFarmerIdAndCrop_IdIn(farmerId, cropIds);
            for (HarvestRecord record : affected) {
                if (record.getCrop() != null) {
                    Long cropId = record.getCrop().getId();
                    record.setArchivedCropName(cropNameMap.getOrDefault(cropId, record.getCrop().getName()));
                    record.setArchivedMeasureUnitName(cropMuMap.get(cropId));
                    record.setCrop(null);
                    record.setArchived(true);
                }
            }
            harvestRecordRepository.saveAll(affected);
            harvestRecordRepository.flush();
            cropRepository.deleteByIdInAndFarmerId(cropIds, farmerId);
        }

        repository.deleteByIdInAndFarmerId(ids, farmerId);
    }

    @Override
    @Transactional
    public void updateActiveBatch(List<Long> ids, Long farmerId, boolean active) {
        if (ids == null || ids.isEmpty()) return;
        repository.updateActiveStatusByIdInAndFarmerId(ids, active, farmerId);
    }

    @Override
    @Transactional
    public Category getOrCreateActiveByName(String name, Long farmerId) {
        String normalized = name == null ? null : name.toUpperCase();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Category name must not be empty");
        }

        Optional<Category> existing = repository.findByNameIgnoreCaseAndFarmerId(normalized, farmerId);
        if (existing.isPresent()) {
            Category cat = existing.get();
            if (!cat.isActive()) {
                cat.setActive(true);
                cat = repository.save(cat);
            }
            return cat;
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        Category newCategory = new Category();
        newCategory.setName(normalized);
        newCategory.setFarmer(farmer);
        newCategory.setActive(true);

        return repository.save(newCategory);
    }

    private Category toEntity(CategoryRequest request, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        Category category = new Category();
        category.setName(request.name().toUpperCase());
        category.setFarmer(farmer);
        category.setActive(request.active() != null ? request.active() : true);
        return category;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive()
        );
    }
}
