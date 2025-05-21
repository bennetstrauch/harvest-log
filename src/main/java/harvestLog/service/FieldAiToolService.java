package harvestLog.service;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldAiToolService {

    private static final Logger logger = LoggerFactory.getLogger(FieldAiToolService.class);

    private final FieldService fieldService;

    public FieldAiToolService(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @Tool(description = "Get a list of all fields for the current farmer.")
    public List<FieldResponse> getAllFields() {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Fetching all fields for farmerId={}", farmerId);
        return fieldService.getAllForFarmer(farmerId);
    }

    @Tool(description = "Get a field by its ID.")
    public FieldResponse getFieldById(Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Fetching field with id={} for farmerId={}", id, farmerId);
        return fieldService.getById(id, farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Field not found or access denied."));
    }

    @Tool(description = "Create a new field with the specified name.")
    public FieldResponse createField(String name) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Creating new field with name='{}' for farmerId={}", name, farmerId);
        return fieldService.create(new FieldRequest(name), farmerId);
    }

    @Tool(description = "Update an existing field by ID with a new name.")
    public FieldResponse updateField(Long id, String newName) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Updating field with id={} to new name='{}' for farmerId={}", id, newName, farmerId);
        return fieldService.update(id, new FieldRequest(newName), farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Field not found or access denied."));
    }

    @Tool(description = "Delete a field by its ID.")
    public String deleteField(Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Attempting to delete field with id={} for farmerId={}", id, farmerId);
        boolean deleted = fieldService.delete(id, farmerId);
        if (!deleted) {
            throw new IllegalArgumentException("Field not found or access denied.");
        }
        return "Field with ID " + id + " was successfully deleted.";
    }

    private Long getAuthenticatedFarmerId() {
        return harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId();
    }
}
