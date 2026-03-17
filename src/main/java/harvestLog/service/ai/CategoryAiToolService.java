package harvestLog.service.ai;

import harvestLog.dto.CategoryRequest;
import harvestLog.dto.CategoryResponse;
import harvestLog.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@Service
public class CategoryAiToolService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryAiToolService.class);

    private final ICategoryService categoryService;

    public CategoryAiToolService(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Tool(description = "Get all active categories for the current farmer.")
    public List<CategoryResponse> getAllCategories() {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Fetching all categories for farmer {}", farmerId);
        return categoryService.getActiveForFarmerId(farmerId);
    }

    @Tool(description = "Create a new category with the given name.")
    public CategoryResponse createCategory(String name) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Creating category '{}' for farmer {}", name, farmerId);
        return categoryService.create(new CategoryRequest(name, true), farmerId);
    }
}
