package harvestLog.service;

import harvestLog.dto.HarvestRecordRequest;
import harvestLog.dto.HarvestRecordResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Component
public class HarvestRecordAiToolService {

    private final HarvestRecordService recordService;

    public HarvestRecordAiToolService(HarvestRecordService recordService) {
        this.recordService = recordService;
    }

    @Tool(description = "Fetch or retrieve all harvest records for the user.")
    public List<HarvestRecordResponse> getAllHarvestRecords() {
        Long farmerId = getAuthenticatedFarmerId();

        System.out.println("Tool fetched records");
        return recordService.getForFarmer(farmerId);
    }

    @Tool(description = "Get harvest records optionally filtered by field IDs, crop IDs, and a date range.")
    public List<HarvestRecordResponse> getFiltered(
            @Parameter(description = "Optional list of field IDs to filter by") List<Long> fieldIds,
            @Parameter(description = "Optional list of crop IDs to filter by") List<Long> cropIds,
            @Parameter(description = "Optional start date in format YYYY-MM-DD") LocalDate startDate,
            @Parameter(description = "Optional end date in format YYYY-MM-DD") LocalDate endDate) {
        Long farmerId = getAuthenticatedFarmerId();
        System.out.println("Tool fetched records");
        return recordService.getFilteredRecords(farmerId, fieldIds, cropIds, startDate, endDate);
    }

    @Tool(description = "Create a new harvest record for farmer.")
    public HarvestRecordResponse createHarvestRecord(
            @Parameter(description = "Harvest record request object") HarvestRecordRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.create(request, farmerId);
    }

    @Tool(description = "Update an existing harvest record by ID.")
    public HarvestRecordResponse updateHarvestRecord(
            @Parameter(description = "ID of the harvest record to update") Long id,
            @Parameter(description = "Harvest record request with updated data") HarvestRecordRequest request) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.update(id, request, farmerId).orElseThrow();
    }

    @Tool(description = "Delete a harvest record by ID.")
    public boolean deleteHarvestRecord(
            @Parameter(description = "ID of the harvest record to delete") Long id) {
        Long farmerId = getAuthenticatedFarmerId();
        return recordService.delete(id, farmerId);
    }

    private Long getAuthenticatedFarmerId() {
        return harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId();
    }
}
