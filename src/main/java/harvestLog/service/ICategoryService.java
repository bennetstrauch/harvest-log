package harvestLog.service;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.model.Category;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<CategoryResponse> getAll(Long farmerId);
    Optional<CategoryResponse> getById(Long id, Long farmerId);
    CategoryResponse create(CategoryRequest request, Long farmerId);

    @Transactional
    List<CategoryResponse> createBatch(List<CategoryRequest> requests, Long farmerId);

    Optional<CategoryResponse> update(Long id, CategoryRequest request, Long farmerId);
    boolean delete(Long id, Long farmerId);

    Category getOrCreateActiveByName(String name, Long farmerId);
}
