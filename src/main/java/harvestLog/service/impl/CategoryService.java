package harvestLog.service.impl;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.model.Category;
import harvestLog.model.Farmer;
import harvestLog.repository.CategoryRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.service.ICategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final FarmerRepository farmerRepository;

    public CategoryService(CategoryRepository categoryRepository, FarmerRepository farmerRepository) {
        this.categoryRepository = categoryRepository;
        this.farmerRepository = farmerRepository;
    }

    @Override
    public List<CategoryResponse> getAll(Long farmerId) {
        return categoryRepository.findByFarmerId(farmerId, Sort.by("name")).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryResponse> getById(Long id, Long farmerId) {
        return categoryRepository.findById(id)
                .filter(cat -> cat.getFarmer().getId().equals(farmerId))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request, Long farmerId) {
        Category entity = toEntity(request, farmerId);
        Category saved = categoryRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public List<CategoryResponse> createBatch(List<CategoryRequest> requests, Long farmerId) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        List<Category> categoriesToSave = requests.stream()
                .map(req -> {
                    String name = req.name().toUpperCase().trim();
                    if (name.isBlank()) {
                        throw new IllegalArgumentException("Category name must not be empty");
                    }

                    // Check if it already exists
                    Optional<Category> existing = categoryRepository.findByNameIgnoreCaseAndFarmerId(name, farmerId);
                    if (existing.isPresent()) {
                        Category cat = existing.get();
                        if (!cat.isActive()) {
                            cat.setActive(true);
                            return categoryRepository.save(cat);
                        } else {
                            return cat;
                        }
                    }

                    // Create new category
                    Category newCategory = new Category();
                    newCategory.setName(name);
                    newCategory.setFarmer(farmer);
                    newCategory.setActive(true);
                    return newCategory;
                })
                .toList();

        // Save all new categories
        List<Category> savedCategories = categoryRepository.saveAll(categoriesToSave);

        // Map to responses
        return savedCategories.stream()
                .map(this::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public Optional<CategoryResponse> update(Long id, CategoryRequest request, Long farmerId) {
        return categoryRepository.findById(id)
                .filter(cat -> cat.getFarmer().getId().equals(farmerId))
                .map(cat -> {
                    cat.setName(request.name().toUpperCase());
                    Category updated = categoryRepository.save(cat);
                    return toResponse(updated);
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long farmerId) {
        return categoryRepository.findById(id)
                .filter(cat -> cat.getFarmer().getId().equals(farmerId))
                .map(cat -> {
                    categoryRepository.delete(cat);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Internal domain method used by other services.
     * Returns a Category entity (reactivating if needed) for the given farmer.
     */
    @Override
    @Transactional
    public Category getOrCreateActiveByName(String name, Long farmerId) {
        String normalized = name == null ? null : name.toUpperCase();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Category name must not be empty");
        }

        Optional<Category> existing = categoryRepository.findByNameIgnoreCaseAndFarmerId(normalized, farmerId);
        if (existing.isPresent()) {
            Category cat = existing.get();
            if (!cat.isActive()) {
                cat.setActive(true);
                cat = categoryRepository.save(cat);
            }
            return cat;
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        Category newCategory = new Category();
        newCategory.setName(normalized);
        newCategory.setFarmer(farmer);
        newCategory.setActive(true);

        return categoryRepository.save(newCategory);
    }

    // ----------------------
    // Mapping helpers
    // ----------------------
    private Category toEntity(CategoryRequest request, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        Category category = new Category();
        category.setName(request.name().toUpperCase());
        category.setFarmer(farmer);
        category.setActive(true);
        return category;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
