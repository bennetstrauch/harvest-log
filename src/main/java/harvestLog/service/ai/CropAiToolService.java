package harvestLog.service.ai;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.service.impl.CropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@Service
public class CropAiToolService {

    private static final Logger logger = LoggerFactory.getLogger(CropAiToolService.class);

    private final CropService cropService;

    public CropAiToolService(CropService cropService) {
        this.cropService = cropService;
    }


    @Tool(description = "Get a list of all crops for the current farmer.")
    public List<CropResponse> getAllCrops() {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Fetching all crops for farmer {}", farmerId);
        return cropService.getAll(farmerId);
    }

    @Tool(description = "Add a new crop by specifying name, measure unit ID, and category ID.")
    public CropResponse addCrop(String name, Long measureUnitId, Long categoryId) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Adding new crop: name='{}', measureUnitId={}, categoryId={}",
                name, measureUnitId, categoryId);

        CropRequest request = new CropRequest(name, measureUnitId, categoryId);
        return cropService.create(request, farmerId);
    }

    @Tool(description = "Update an existing crop by its ID. Provide the new name, measure unit ID, and category ID.")
    public CropResponse updateCrop(Long id, String name, Long measureUnitId, Long categoryId) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Updating crop: id={}, name='{}', measureUnitId={}, categoryId={}",
                id, name, measureUnitId, categoryId);

        CropRequest request = new CropRequest(name, measureUnitId, categoryId);
        return cropService.update(id, request, farmerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Crop not found or not owned by farmer"));
    }


//    @Tool(description = "Search for crops by category name.")
//    public List<CropResponse> searchByCategory(String categoryName) {
//        Long farmerId = getAuthenticatedFarmerId();
//        logger.info("Searching crops by category '{}' for farmer {}", categoryName, farmerId);
//        return cropService.searchByCategoryName(categoryName, farmerId);
//    }

//    @Tool(description = "Get crops by name (substring match).")
//    public List<CropResponse> searchByName(String namePart) {
//        Long farmerId = getAuthenticatedFarmerId();
//        logger.info("Searching crops by name containing '{}' for farmer {}", namePart, farmerId);
//        Optional<List<CropResponse>> results = cropService.findByNameContains(namePart, farmerId);
//        return results.orElse(List.of());
//    }

//    @Tool(description = "Get all harvest records for a given crop ID.")
//    public List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId) {
//        Long farmerId = getAuthenticatedFarmerId();
//        logger.info("Fetching harvests for cropId={} and farmer {}", cropId, farmerId);
//        return cropService.getHarvestsByCrop(cropId, farmerId);
//    }

    @Tool(description = "Delete a crop by its ID")
    public String deleteCrop(Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Deleting crop with id={} for farmer {}", id, farmerId);

        boolean deleted = cropService.delete(id, farmerId);
        if (deleted) {
            return "Crop with ID " + id + " was successfully deleted.";
        } else {
            throw new IllegalArgumentException("Crop not found or not owned by farmer");
        }
    }
}
