package harvestLog.service;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.model.Category;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<CategoryResponse> getAllForFarmerId(Long farmerId);
    List<CategoryResponse> getActiveForFarmerId(Long farmerId);
    List<CategoryResponse> getInactiveForFarmerId(Long farmerId);
    List<CategoryResponse> getAllForFarmerId(Long farmerId, Boolean active);

    Optional<CategoryResponse> getById(Long id, Long farmerId);

    CategoryResponse create(CategoryRequest request, Long farmerId);
    List<CategoryResponse> createBatch(List<CategoryRequest> requests, Long farmerId);
    Optional<CategoryResponse> update(Long id, CategoryRequest request, Long farmerId);

    boolean delete(Long id, Long farmerId);
    int deleteBatch(List<Long> ids, Long farmerId);
    void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade);
    void updateActiveBatch(List<Long> ids, Long farmerId, boolean active);

    Category getOrCreateActiveByName(String name, Long farmerId);
}
