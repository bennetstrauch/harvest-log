package harvestLog.service;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getById(Long id);

    CategoryResponse create(CategoryRequest request);

    Optional<CategoryResponse> update(Long id, CategoryRequest request);

    boolean delete(Long id);
}
