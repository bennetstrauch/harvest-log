package harvestLog.service.impl;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.model.Category;
import harvestLog.model.MeasureUnit;
import harvestLog.repository.CategoryRepository;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.service.ICropService;
import harvestLog.service.ICategoryService;
import harvestLog.service.ai.CategoryAiService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropService implements ICropService {

    private final CropRepository cropRepo;
    private final FarmerRepository farmerRepo;
    private final ICategoryService categoryService;
    private final MeasureUnitRepository measureUnitRepo;
    private final CategoryRepository categoryRepo;
    private final CategoryAiService categoryAiService;

    public CropService(
            CropRepository cropRepo,
            FarmerRepository farmerRepo,
            ICategoryService categoryService,
            MeasureUnitRepository measureUnitRepo,
            CategoryRepository categoryRepo,
            CategoryAiService categoryAiService
    ) {
        this.cropRepo = cropRepo;
        this.farmerRepo = farmerRepo;
        this.categoryService = categoryService;
        this.measureUnitRepo = measureUnitRepo;
        this.categoryRepo = categoryRepo;
        this.categoryAiService = categoryAiService;
    }

    @Override
    public List<CropResponse> getAll(Long farmerId) {
        return cropRepo.findByFarmerId(farmerId).stream()
                .map(this::toResponse)
                .toList();
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
            List<CategoryAiService.SuggestionResult> aiResults =
                    categoryAiService.suggestForBatch(namesForAi, existingNames);

            for (int j = 0; j < aiResults.size(); j++) {
                int origIndex = aiIndices.get(j);
                PerCropMeta meta = metas.get(origIndex);
                CategoryAiService.SuggestionResult r = aiResults.get(j);

                if (r == null) continue;

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
                    } else {
                        meta.suggestion = chosen;
                    }
                } else if (r.suggestion != null && !r.suggestion.isBlank()) {
                    meta.suggestion = r.suggestion.trim();
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
        return cropRepo.findById(id)
                .filter(c -> c.getFarmer().getId().equals(farmerId))
                .map(c -> {
                    cropRepo.delete(c);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public int deleteBatch(List<Long> ids, Long farmerId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // Use a single query to find and delete crops belonging to the farmer
        int deletedCount = cropRepo.deleteByIdInAndFarmerId(ids, farmerId);
        return deletedCount;
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
                crop.getCategory() != null ? crop.getCategory().getName() : null,
                true,   // always resolved in normal reads
                null
        );
    }
}
