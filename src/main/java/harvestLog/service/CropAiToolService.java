package harvestLog.service;

import harvestLog.dto.CropRequest;
import harvestLog.dto.CropResponse;
import harvestLog.dto.HarvestSummaryResponse;
import harvestLog.model.Category;
import harvestLog.model.MeasureUnit;
import harvestLog.service.impl.CropService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CropAiToolService {

    private final CropService cropService;

    public CropAiToolService(CropService cropService) {
        this.cropService = cropService;
    }

    @Tool(description = "Add a new crop by specifying name, measure unit (e.g. pounds or pieces), and category.")
    public CropResponse addCrop(String name, String measureUnit, String category) {
        return cropService.addCrop(new CropRequest(name, parseMeasureUnit(measureUnit), parseCategory(category)));
    }

    @Tool(description = "Get a list of all crops for the farmer.")
    public List<CropResponse> getAllCrops() {
        return cropService.getAllCrops();
    }

    @Tool(description = "Search for crops by category.")
    public List<CropResponse> searchByCategory(String category) {
        return cropService.searchByCategory(parseCategory(category));
    }

    @Tool(description = "Get a crop by its name.")
    public CropResponse getCropByName(String cropName) {
        return cropService.getCropByName(cropName);
    }

    @Tool(description = "Get all harvest records for a given crop ID.")
    public List<HarvestSummaryResponse> getHarvestsByCrop(Long cropId) {
        return cropService.getHarvestsByCrop(cropId);
    }

    @Tool(description = "Update an existing crop by its ID. Provide the new name, measure unit (e.g. POUNDS, PIECES), and category (e.g. VEGETABLE, FRUIT, etc.)")
    public CropResponse updateCrop(Long id, String name, String measureUnit, String category) {
        CropRequest request = new CropRequest(name, parseMeasureUnit(measureUnit), parseCategory(category));
        return cropService.updateCrop(id, request);
    }

    @Tool(description = "Delete a crop by its ID")
    public String deleteCrop(Long id) {
        cropService.deleteCrop(id);
        return "Crop with ID " + id + " was successfully deleted.";
    }


    private Category parseCategory(String category) {
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category. Valid options: " + List.of(Category.values()));
        }
    }

    private MeasureUnit parseMeasureUnit(String unit) {
        try {
            return MeasureUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid measure unit. Valid options: " + List.of(MeasureUnit.values()));
        }
    }
}
