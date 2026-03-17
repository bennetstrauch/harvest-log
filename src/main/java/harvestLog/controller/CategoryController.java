package harvestLog.controller;

import harvestLog.dto.BatchActiveRequest;
import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.dto.HardDeleteRequest;
import harvestLog.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) Boolean active
    ) {
        Long farmerId = getAuthenticatedFarmerId();
        return ResponseEntity.ok(categoryService.getAllForFarmerId(farmerId, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return categoryService.getById(id, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        CategoryResponse created = categoryService.create(request, farmerId);
        return ResponseEntity.status(201).body(created);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CategoryResponse>> createBatch(@Valid @RequestBody List<CategoryRequest> requests) {
        Long farmerId = getAuthenticatedFarmerId();
        List<CategoryResponse> created = categoryService.createBatch(requests, farmerId);
        return ResponseEntity.status(201).body(created);
    }



    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return categoryService.update(id, request, farmerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        boolean deleted = categoryService.delete(id, farmerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        Long farmerId = getAuthenticatedFarmerId();
        int deletedCount = categoryService.deleteBatch(ids, farmerId);
        return deletedCount > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/batch/hard")
    public ResponseEntity<Void> hardDeleteBatch(@RequestBody HardDeleteRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        categoryService.hardDeleteBatch(request.ids(), farmerId, request.cascade());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/batch/active")
    public ResponseEntity<Void> updateActiveBatch(@RequestBody BatchActiveRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        categoryService.updateActiveBatch(request.ids(), farmerId, request.active());
        return ResponseEntity.noContent().build();
    }
}
