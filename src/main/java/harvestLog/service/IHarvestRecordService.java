package harvestLog.service;

import harvestLog.dto.HarvestRecordRequest;
import harvestLog.dto.HarvestRecordResponse;
import harvestLog.model.HarvestRecord;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing harvest records.
 */
public interface IHarvestRecordService {

    /**
     * Retrieves all harvest records.
     * @return List of all HarvestRecord entities
     */
    List<HarvestRecord> getAllHarvestRecords();

    /**
     * Retrieves harvest records for a specific farmer.
     * @param farmerId ID of the farmer
     * @return List of HarvestRecordResponse DTOs
     */
    List<HarvestRecordResponse> getForFarmer(Long farmerId);

    /**
     * Retrieves filtered harvest records based on specified criteria.
     * @param farmerId ID of the farmer
     * @param fieldIds List of field IDs to filter by
     * @param cropIds List of crop IDs to filter by
     * @param startDate Start date for filtering
     * @param endDate End date for filtering
     * @return List of filtered HarvestRecordResponse DTOs
     */
    List<HarvestRecordResponse> getFilteredRecords(Long farmerId, List<Long> fieldIds, List<Long> cropIds, LocalDate startDate, LocalDate endDate);

    /**
     * Returns the latest HarvestRecord of a farmer.
     * @param farmerId -
     * @return HarvestRecordDTO
     */
    Optional<HarvestRecordResponse> getLatestForFarmer(Long farmerId);

    /**
     * Creates a new harvest record for a farmer.
     * @param request Harvest record request DTO
     * @param farmerId ID of the farmer
     * @return Created HarvestRecordResponse DTO
     */
    HarvestRecordResponse create(@Valid HarvestRecordRequest request, Long farmerId);

    /**
     * Updates an existing harvest record.
     * @param id ID of the harvest record to update
     * @param request Harvest record request DTO with updated values
     * @param farmerId ID of the farmer
     * @return Optional containing the updated HarvestRecordResponse DTO, or empty if not found or unauthorized
     */
    Optional<HarvestRecordResponse> update(Long id, @Valid HarvestRecordRequest request, Long farmerId);

    /**
     * Deletes a harvest record.
     * @param harvestEntryId ID of the harvest record to delete
     * @param farmerId ID of the farmer
     * @return true if the record was deleted successfully, false otherwise
     */
    boolean delete(Long harvestEntryId, Long farmerId);
}