package harvestLog.service.impl;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.DependencyConflictResponse;
import harvestLog.exception.AlreadyExistsException;
import harvestLog.exception.DependencyConflictException;
import harvestLog.model.Crop;
import harvestLog.plan.PlanLimits;
import harvestLog.service.PlanService;
import harvestLog.model.Farmer;
import harvestLog.model.Category;
import harvestLog.model.HarvestRecord;
import harvestLog.model.MeasureUnit;
import harvestLog.repository.CategoryRepository;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.HarvestRecordRepository;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.service.ICropService;
import harvestLog.service.ICategoryService;
import harvestLog.service.ai.CategoryAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropService implements ICropService {

    private static final Logger logger = LoggerFactory.getLogger(CropService.class);

    private final CropRepository cropRepo;
    private final FarmerRepository farmerRepo;
    private final ICategoryService categoryService;
    private final MeasureUnitRepository measureUnitRepo;
    private final CategoryRepository categoryRepo;
    private final CategoryAiService categoryAiService;
    private final HarvestRecordRepository harvestRecordRepo;
    private final PlanService planService;

    public CropService(
            CropRepository cropRepo,
            FarmerRepository farmerRepo,
            ICategoryService categoryService,
            MeasureUnitRepository measureUnitRepo,
            CategoryRepository categoryRepo,
            CategoryAiService categoryAiService,
            HarvestRecordRepository harvestRecordRepo,
            PlanService planService
    ) {
        this.cropRepo = cropRepo;
        this.farmerRepo = farmerRepo;
        this.categoryService = categoryService;
        this.measureUnitRepo = measureUnitRepo;
        this.categoryRepo = categoryRepo;
        this.categoryAiService = categoryAiService;
        this.harvestRecordRepo = harvestRecordRepo;
        this.planService = planService;
    }

    @Override
    public List<CropResponse> getAll(Long farmerId) {
        return cropRepo.findByFarmerId(farmerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CropResponse> getAllActive(Long farmerId) {
        return cropRepo.findByFarmerIdAndActive(farmerId, true).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CropResponse> getAllInactive(Long farmerId) {
        return cropRepo.findByFarmerIdAndActive(farmerId, false).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CropResponse> getAll(Long farmerId, Boolean active) {
        if (active == null) {
            return getAll(farmerId); // All entities
        }
        return active ? getAllActive(farmerId) : getAllInactive(farmerId);
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
        System.out.println("Creating crop: " + request.name() + " for farmer: " + farmerId);
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        planService.enforceLimit(farmer, cropRepo.countByFarmerIdAndActiveTrue(farmerId), 1, PlanLimits.FREE_MAX_CROPS, "crops");
        try {
            Crop crop = toEntity(request, farmerId);
            Crop saved = cropRepo.save(crop);
            System.out.println("Successfully created crop: " + saved.getName());
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException caught: " + e.getMessage());
            return handleDuplicateCrop(request, farmerId);
        } catch (Exception e) {
            System.out.println("Other exception caught: " + e.getClass().getName() + " - " + e.getMessage());
            // Check if it's a constraint violation in the nested causes
            Throwable cause = e.getCause();
            while (cause != null) {
                System.out.println("Cause: " + cause.getClass().getName() + " - " + cause.getMessage());
                if (cause.getMessage() != null && cause.getMessage().contains("crop_farmer_id_name_key")) {
                    System.out.println("Found constraint violation in nested cause, handling duplicate");
                    return handleDuplicateCrop(request, farmerId);
                }
                cause = cause.getCause();
            }
            throw e; // Re-throw if not a known constraint violation
        }
    }

    private CropResponse handleDuplicateCrop(CropRequest request, Long farmerId) {
        System.out.println("Handling duplicate crop for: " + request.name() + " (farmer: " + farmerId + ")");

        Optional<Crop> existing = cropRepo.findByNameIgnoreCaseAndFarmerId(request.name(), farmerId);
        System.out.println("Found existing crop: " + existing.isPresent());

        if (existing.isPresent()) {
            Crop crop = existing.get();
            System.out.println("Existing crop active status: " + crop.isActive());

            if (!crop.isActive()) {
                System.out.println("Reactivating inactive crop: " + crop.getName());
                crop.setActive(true);

                // Update other fields from the request
                if (request.measureUnitId() != null) {
                    MeasureUnit mu = measureUnitRepo.findById(request.measureUnitId())
                            .orElseThrow(() -> new IllegalArgumentException("Measure unit not found: " + request.measureUnitId()));
                    crop.setMeasureUnit(mu);
                }

                if (request.categoryId() != null) {
                    Category cat = categoryRepo.findById(request.categoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
                    crop.setCategory(cat);
                }

                Crop saved = cropRepo.save(crop);
                System.out.println("Successfully reactivated crop: " + saved.getName() + " (active: " + saved.isActive() + ")");
                return toResponse(saved);
            } else {
                System.out.println("Crop is already active, throwing exception");
                throw new AlreadyExistsException("Active crop with name '" + request.name() + "' already exists");
            }
        }

        System.out.println("No existing crop found, this shouldn't happen in duplicate handling");
        throw new AlreadyExistsException("Crop with name '" + request.name() + "' could not be processed");
    }


    @Override
    @Transactional
    public List<CropResponse> createBatch(List<CropRequest> requests, Long farmerId) {
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        planService.enforceLimit(farmer, cropRepo.countByFarmerIdAndActiveTrue(farmerId), requests.size(), PlanLimits.FREE_MAX_CROPS, "crops");
        // load categories once for performance
        List<Category> existingCats = categoryRepo.findByFarmerId(farmerId, Sort.by("name"));
        Map<Long, Category> categoriesById = existingCats.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<String> existingNames = existingCats.stream()
                .map(Category::getName)
                .toList();

        // per-crop meta
        final class PerCropMeta {
            Long assignedCategoryId;
            String assignedCategoryName;
            boolean resolved;
            String suggestion;
        }

        List<PerCropMeta> metas = new ArrayList<>(requests.size());
        List<Crop> crops = new ArrayList<>(requests.size());
        List<Integer> aiIndices = new ArrayList<>();
        List<String> namesForAi = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CropRequest req = requests.get(i);
            PerCropMeta meta = new PerCropMeta();
            metas.add(meta);

            Crop crop = toEntityWithoutCategory(req, farmerId);

            if (req.categoryId() != null) {
                Category cat = categoriesById.get(req.categoryId());
                if (cat != null) {
                    crop.setCategory(cat);
                    meta.assignedCategoryId = cat.getId();
                    meta.assignedCategoryName = cat.getName();
                    meta.resolved = true;
                }
            }

            if (!meta.resolved) {
                aiIndices.add(i);
                namesForAi.add(req.name());
            }

            crops.add(crop);
        }

        // batch AI call for unresolved crops
        if (!namesForAi.isEmpty()) {
            logger.info("Calling CategoryAiService for {} crop(s): {} | existing categories: {}",
                    namesForAi.size(), namesForAi, existingNames);

            List<CategoryAiService.SuggestionResult> aiResults =
                    categoryAiService.suggestForBatch(namesForAi, existingNames);

            logger.info("CategoryAiService returned {} result(s)", aiResults.size());

            for (int j = 0; j < aiResults.size(); j++) {
                int origIndex = aiIndices.get(j);
                PerCropMeta meta = metas.get(origIndex);
                CategoryAiService.SuggestionResult r = aiResults.get(j);
                String cropName = namesForAi.get(j);

                if (r == null) {
                    logger.warn("AI result null for crop '{}'", cropName);
                    continue;
                }

                logger.info("AI result for '{}': matchedExisting='{}', suggestion='{}'",
                        cropName, r.matchedExisting, r.suggestion);

                if (r.matchedExisting != null) {
                    String chosen = r.matchedExisting.trim();
                    Category matched = existingCats.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(chosen))
                            .findFirst()
                            .orElse(null);
                    if (matched != null) {
                        crops.get(origIndex).setCategory(matched);
                        meta.assignedCategoryId = matched.getId();
                        meta.assignedCategoryName = matched.getName();
                        meta.resolved = true;
                        logger.info("Crop '{}' matched existing category '{}'", cropName, matched.getName());
                    } else {
                        meta.suggestion = chosen;
                        logger.info("Crop '{}' AI chose '{}' but no match found in existing — treating as suggestion", cropName, chosen);
                    }
                } else if (r.suggestion != null && !r.suggestion.isBlank()) {
                    meta.suggestion = r.suggestion.trim();
                    logger.info("Crop '{}' has no matching category — AI suggests new: '{}'", cropName, meta.suggestion);
                } else {
                    logger.warn("Crop '{}' — AI returned no suggestion and no match", cropName);
                }
            }
        }

        List<Crop> saved = cropRepo.saveAll(crops);

        List<CropResponse> responses = new ArrayList<>(saved.size());
        for (int i = 0; i < saved.size(); i++) {
            Crop s = saved.get(i);
            PerCropMeta m = metas.get(i);

            responses.add(new CropResponse(
                    s.getId(),
                    s.getName(),
                    s.getMeasureUnit() != null ? s.getMeasureUnit().getId() : null,
                    s.getCategory() != null ? s.getCategory().getId() : m.assignedCategoryId,
                    s.isActive(),
                    s.getCategory() != null ? s.getCategory().getName() : m.assignedCategoryName,
                    m.resolved,
                    m.suggestion

            ));
        }
        return responses;
    }

    @Override
    @Transactional
    public Optional<CropResponse> update(Long id, CropRequest request, Long farmerId) {
        System.out.println("UPDATE CROP - ID: " + id + ", Request: " + request + ", FarmerID: " + farmerId);
        return cropRepo.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmerId))
                .map(c -> {
                    System.out.println("BEFORE UPDATE - Crop: " + c.getName() + ", MeasureUnit: " +
                        (c.getMeasureUnit() != null ? c.getMeasureUnit().getId() : "null"));

                    // Only update fields that are provided (not null)
                    if (request.name() != null) {
                        c.setName(request.name());
                        System.out.println("Updated name to: " + request.name());
                    }

                    if (request.active() != null) {
                        c.setActive(request.active());
                        System.out.println("Updated active status to: " + request.active());
                    }

                    if (request.measureUnitId() != null) {
                        System.out.println("Updating measureUnitId to: " + request.measureUnitId());
                        MeasureUnit mu = measureUnitRepo.findById(request.measureUnitId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Measure unit not found: " + request.measureUnitId()));
                        c.setMeasureUnit(mu);
                        System.out.println("MeasureUnit found and set: " + mu.getName() + " (ID: " + mu.getId() + ")");
                    }

                    if (request.categoryId() != null) {
                        System.out.println("Updating categoryId to: " + request.categoryId());
                        Category cat = categoryRepo.findById(request.categoryId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Category not found: " + request.categoryId()));
                        c.setCategory(cat);
                        System.out.println("Category found and set: " + cat.getName() + " (ID: " + cat.getId() + ")");
                    } else if (request.categoryId() == null && request.name() != null) {
                        // Only clear category if this is a complete update (name provided)
                        c.setCategory(null);
                        System.out.println("Category cleared");
                    }

                    Crop saved = cropRepo.save(c);
                    System.out.println("AFTER SAVE - Crop: " + saved.getName() + ", MeasureUnit: " +
                        (saved.getMeasureUnit() != null ? saved.getMeasureUnit().getId() : "null"));

                    CropResponse response = toResponse(saved);
                    System.out.println("RESPONSE - " + response);
                    return response;
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long farmerId) {
        return cropRepo.softDeleteByIdInAndFarmerId(List.of(id), farmerId) > 0;
    }

    @Override
    @Transactional
    public int deleteBatch(List<Long> ids, Long farmerId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // Use soft deletion instead of hard deletion
        return cropRepo.softDeleteByIdInAndFarmerId(ids, farmerId);
    }

    @Override
    @Transactional
    public void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade) {
        if (ids == null || ids.isEmpty()) return;

        List<Crop> crops = cropRepo.findAllById(ids).stream()
                .filter(c -> c.getFarmer().getId().equals(farmerId))
                .collect(Collectors.toList());

        if (crops.isEmpty()) return;

        List<Long> validIds = crops.stream().map(Crop::getId).collect(Collectors.toList());

        int affectedCount = harvestRecordRepo.countByFarmerIdAndCrop_IdIn(farmerId, validIds);

        if (affectedCount > 0 && !cascade) {
            throw new DependencyConflictException(new DependencyConflictResponse(
                    "This crop is referenced by harvest records",
                    "DEPENDENCY_CONFLICT",
                    List.of(),
                    affectedCount,
                    LocalDateTime.now()
            ));
        }

        if (cascade && affectedCount > 0) {
            Map<Long, String> cropNameMap = crops.stream()
                    .collect(Collectors.toMap(Crop::getId, Crop::getName));
            Map<Long, String> cropMuMap = crops.stream()
                    .filter(c -> c.getMeasureUnit() != null)
                    .collect(Collectors.toMap(Crop::getId, c -> {
                        var mu = c.getMeasureUnit();
                        return (mu.getAbbreviation() != null && !mu.getAbbreviation().isBlank())
                                ? mu.getAbbreviation() : mu.getName();
                    }));
            List<HarvestRecord> affected = harvestRecordRepo.findByFarmerIdAndCrop_IdIn(farmerId, validIds);
            for (HarvestRecord record : affected) {
                if (record.getCrop() != null) {
                    Long cropId = record.getCrop().getId();
                    record.setArchivedCropName(cropNameMap.getOrDefault(cropId, record.getCrop().getName()));
                    record.setArchivedMeasureUnitName(cropMuMap.get(cropId));
                    record.setCrop(null);
                    record.setArchived(true);
                }
            }
            harvestRecordRepo.saveAll(affected);
            harvestRecordRepo.flush();
        }

        cropRepo.deleteByIdInAndFarmerId(validIds, farmerId);
    }

    @Override
    @Transactional
    public void updateActiveBatch(List<Long> ids, Long farmerId, boolean active) {
        if (ids == null || ids.isEmpty()) return;
        cropRepo.updateActiveStatusByIdInAndFarmerId(ids, active, farmerId);
    }

    // ===== Mapping helpers =====

    private Crop toEntity(CropRequest request, Long farmerId) {
        Crop crop = toEntityWithoutCategory(request, farmerId);

        if (request.categoryId() != null) {
            Category cat = categoryRepo.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Category not found: " + request.categoryId()));
            crop.setCategory(cat);
        }
        return crop;
    }

    /** Helper used in batch creation, skips category resolution. */
    private Crop toEntityWithoutCategory(CropRequest request, Long farmerId) {
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        Crop crop = new Crop();
        crop.setName(request.name());
        crop.setFarmer(farmer);

        // Set active status (default to true if not specified)
        crop.setActive(request.active() != null ? request.active() : true);

        if (request.measureUnitId() == null) {
            throw new IllegalArgumentException("measureUnitId is required for crop: " + request.name());
        }
        MeasureUnit mu = measureUnitRepo.findById(request.measureUnitId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Measure unit not found: " + request.measureUnitId()));
        crop.setMeasureUnit(mu);

        return crop;
    }

    private CropResponse toResponse(Crop crop) {
        return new CropResponse(
                crop.getId(),
                crop.getName(),
                crop.getMeasureUnit() != null ? crop.getMeasureUnit().getId() : null,
                crop.getCategory() != null ? crop.getCategory().getId() : null,
                crop.isActive(),
                crop.getCategory() != null ? crop.getCategory().getName() : null,
                true,   // always resolved in normal reads
                null
        );
    }
}
