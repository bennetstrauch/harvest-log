package harvestLog.service.impl;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.model.Category;
import harvestLog.repository.CategoryRepository;
import harvestLog.service.ICategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id " + id));
        return new CategoryResponse(category.getId(), category.getName());
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category(request.name());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @Override
    public Optional<CategoryResponse> update(Long id, CategoryRequest request) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.name().toUpperCase());
                    Category updated = categoryRepository.save(existing);
                    return new CategoryResponse(updated.getId(), updated.getName());
                });
    }

    @Override
    public boolean delete(Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.delete(category);
                    return true;
                })
                .orElse(false);
    }
}
