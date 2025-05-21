package harvestLog.service.impl;

import harvestLog.dto.HarvestRecordRequest;
import harvestLog.dto.HarvestRecordResponse;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.model.Field;
import harvestLog.model.HarvestRecord;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.FieldRepository;
import harvestLog.repository.HarvestRecordRepo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HarvestRecordService {
    private final HarvestRecordRepo recordRepo;
    private final CropRepository cropRepo;
    private final FieldRepository fieldRepo;
    private final FarmerRepository farmerRepo;

    public HarvestRecordService(HarvestRecordRepo recordRepo, CropRepository cropRepo,
                                FieldRepository fieldRepo, FarmerRepository farmerRepo) {
        this.recordRepo = recordRepo;
        this.cropRepo = cropRepo;
        this.fieldRepo = fieldRepo;
        this.farmerRepo = farmerRepo;
    }

    public List<HarvestRecordResponse> getForFarmer(Long farmerId) {
        return recordRepo.findByFarmerId(farmerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HarvestRecordResponse> getFilteredRecords(Long farmerId, List<Long> fieldIds, List<Long> cropIds,
                                                          LocalDate startDate, LocalDate endDate) {
        return recordRepo.findAll((Root<HarvestRecord> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    if (farmerId != null) {
                        predicates.add(cb.equal(root.get("farmer").get("id"), farmerId));
                    }
                    if (fieldIds != null && !fieldIds.isEmpty()) {
                        predicates.add(root.join("fields").get("id").in(fieldIds));
                    }
                    if (cropIds != null && !cropIds.isEmpty()) {
                        predicates.add(root.get("crop").get("id").in(cropIds));
                    }
                    if (startDate != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
                    }
                    if (endDate != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                }).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HarvestRecordResponse create(HarvestRecordRequest request, Long farmerId) {
        try {
            HarvestRecord record = toEntity(request, farmerId);
            HarvestRecord saved = recordRepo.save(record);
            return toResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create harvest record: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Optional<HarvestRecordResponse> update(Long id, HarvestRecordRequest request, Long farmerId) {
        return recordRepo.findById(id)
                .filter(record -> record.getFarmer().getId().equals(farmerId))
                .map(record -> {
                    updateEntity(record, request);
                    return toResponse(recordRepo.save(record));
                });
    }

    @Transactional
    public boolean delete(Long harvestEntryId, Long farmerId) {
        Optional<HarvestRecord> record = recordRepo.findById(harvestEntryId);
        if (record.isPresent() && record.get().getFarmer().getId().equals(farmerId)) {
            recordRepo.deleteById(harvestEntryId);
            return true;
        }
        return false;
    }

    private HarvestRecord toEntity(HarvestRecordRequest request, Long farmerId) {
        Crop crop = cropRepo.findById(request.cropId())
                .orElseThrow(() -> new IllegalArgumentException("Crop not found: " + request.cropId()));
        List<Field> fields = fieldRepo.findAllById(request.fieldIds());
        if (fields.size() != request.fieldIds().size()) {
            throw new IllegalArgumentException("One or more fields not found");
        }

     
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        HarvestRecord record = new HarvestRecord();
        record.setDate(request.date());
        record.setCrop(crop);
        record.setFields(fields);
        record.setHarvestedQuantity(request.harvestedQuantity());
        record.setFarmer(farmer);
        return record;
    }

    private void updateEntity(HarvestRecord record, HarvestRecordRequest request) {
        Crop crop = cropRepo.findById(request.cropId())
                .orElseThrow(() -> new IllegalArgumentException("Crop not found: " + request.cropId()));
        List<Field> fields = fieldRepo.findAllById(request.fieldIds());
        if (fields.size() != request.fieldIds().size()) {
            throw new IllegalArgumentException("One or more fields not found");
        }
        // Verify fields belong to the farmer
        fields.forEach(field -> {
            if (!field.getFarmer().getId().equals(record.getFarmer().getId())) {
                throw new IllegalArgumentException("Field " + field.getId() + " does not belong to farmer " + record.getFarmer().getId());
            }
        });

        record.setDate(request.date());
        record.setCrop(crop);
        record.setFields(fields);
        record.setHarvestedQuantity(request.harvestedQuantity());
    }

    private HarvestRecordResponse toResponse(HarvestRecord record) {
        return new HarvestRecordResponse(
                record.getId(),
                record.getDate(),
                record.getCrop().getId(),
                record.getFields().stream().map(Field::getId).collect(Collectors.toList()),
                record.getHarvestedQuantity(),
                record.getFarmer().getId()
        );
    }
}