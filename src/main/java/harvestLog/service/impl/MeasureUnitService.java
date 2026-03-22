package harvestLog.service.impl;

import harvestLog.dto.DependencyConflictResponse;
import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.exception.AlreadyExistsException;
import harvestLog.exception.DependencyConflictException;
import harvestLog.plan.PlanLimits;
import harvestLog.service.PlanService;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import harvestLog.model.HarvestRecord;
import harvestLog.model.MeasureUnit;
import harvestLog.repository.CropRepository;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.HarvestRecordRepository;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.service.IMeasureUnitService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeasureUnitService implements IMeasureUnitService {

    private final MeasureUnitRepository repository;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final HarvestRecordRepository harvestRecordRepository;
    private final PlanService planService;

    public MeasureUnitService(MeasureUnitRepository repository, FarmerRepository farmerRepository,
                              CropRepository cropRepository, HarvestRecordRepository harvestRecordRepository,
                              PlanService planService) {
        this.repository = repository;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.harvestRecordRepository = harvestRecordRepository;
        this.planService = planService;
    }

    @Override
    public List<MeasureUnitResponse> getAllForFarmerId(Long farmerId) {
        return repository.findAllByFarmer_Id(farmerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MeasureUnitResponse> getActiveForFarmerId(Long farmerId) {
        return repository.findByFarmerIdAndActive(farmerId, true).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MeasureUnitResponse> getInactiveForFarmerId(Long farmerId) {
        return repository.findByFarmerIdAndActive(farmerId, false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MeasureUnitResponse> getAllForFarmerId(Long farmerId, Boolean active) {
        if (active == null) {
            return getAllForFarmerId(farmerId);
        }
        return active ? getActiveForFarmerId(farmerId) : getInactiveForFarmerId(farmerId);
    }

    @Override
    public Optional<MeasureUnitResponse> getById(Long id, Long farmerId) {
        return repository.findById(id)
                .filter(unit -> unit.getFarmer().getId().equals(farmerId))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public MeasureUnitResponse create(MeasureUnitRequest request, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        planService.enforceLimit(farmer, repository.countByFarmerIdAndActiveTrue(farmerId), 1, PlanLimits.FREE_MAX_MEASURE_UNITS, "measure units");
        try {
            MeasureUnit unit = toEntity(request, farmerId);
            MeasureUnit saved = repository.save(unit);
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            return handleDuplicateUnit(request, farmerId);
        }
    }

    private MeasureUnitResponse handleDuplicateUnit(MeasureUnitRequest request, Long farmerId) {
        Optional<MeasureUnit> existing = repository.findByNameIgnoreCaseAndFarmerId(request.name(), farmerId);

        if (existing.isPresent() && !existing.get().isActive()) {
            MeasureUnit unit = existing.get();
            unit.setActive(true);
            if (request.abbreviation() != null) {
                unit.setAbbreviation(request.abbreviation());
            }
            return toResponse(repository.save(unit));
        }

        throw new AlreadyExistsException("Active measure unit with name '" + request.name() + "' already exists");
    }

    @Override
    @Transactional
    public List<MeasureUnitResponse> createBatch(List<MeasureUnitRequest> requests, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        planService.enforceLimit(farmer, repository.countByFarmerIdAndActiveTrue(farmerId), requests.size(), PlanLimits.FREE_MAX_MEASURE_UNITS, "measure units");

        List<MeasureUnit> units = requests.stream()
                .map(request -> {
                    MeasureUnit unit = new MeasureUnit();
                    unit.setName(request.name());
                    unit.setAbbreviation(request.abbreviation());
                    unit.setFarmer(farmer);
                    unit.setActive(request.active() != null ? request.active() : true);
                    return unit;
                })
                .collect(Collectors.toList());

        List<MeasureUnit> saved = repository.saveAll(units);
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<MeasureUnitResponse> update(Long id, MeasureUnitRequest request, Long farmerId) {
        return repository.findById(id)
                .filter(unit -> unit.getFarmer().getId().equals(farmerId))
                .map(unit -> {
                    if (request.name() != null) {
                        unit.setName(request.name());
                    }
                    if (request.abbreviation() != null) {
                        unit.setAbbreviation(request.abbreviation());
                    }
                    if (request.active() != null) {
                        unit.setActive(request.active());
                    }
                    return toResponse(repository.save(unit));
                });
    }

    @Override
    @Transactional
    public boolean delete(Long id, Long farmerId) {
        return repository.softDeleteByIdInAndFarmerId(List.of(id), farmerId) > 0;
    }

    @Override
    @Transactional
    public int deleteBatch(List<Long> ids, Long farmerId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return repository.softDeleteByIdInAndFarmerId(ids, farmerId);
    }

    @Override
    @Transactional
    public void hardDeleteBatch(List<Long> ids, Long farmerId, boolean cascade) {
        if (ids == null || ids.isEmpty()) return;

        List<Crop> affectedCrops = cropRepository.findByMeasureUnit_IdInAndFarmerId(ids, farmerId);

        if (!affectedCrops.isEmpty() && !cascade) {
            List<Long> cropIds = affectedCrops.stream().map(Crop::getId).collect(Collectors.toList());
            int hrCount = harvestRecordRepository.countByFarmerIdAndCrop_IdIn(farmerId, cropIds);
            List<DependencyConflictResponse.EntitySummary> cropSummaries = affectedCrops.stream()
                    .map(c -> new DependencyConflictResponse.EntitySummary(c.getId(), c.getName()))
                    .collect(Collectors.toList());
            throw new DependencyConflictException(new DependencyConflictResponse(
                    "This measure unit is used by crops which may have harvest records",
                    "DEPENDENCY_CONFLICT",
                    cropSummaries,
                    hrCount,
                    LocalDateTime.now()
            ));
        }

        if (!affectedCrops.isEmpty()) {
            List<Long> cropIds = affectedCrops.stream().map(Crop::getId).collect(Collectors.toList());
            Map<Long, String> cropNameMap = affectedCrops.stream()
                    .collect(Collectors.toMap(Crop::getId, Crop::getName));
            Map<Long, String> cropMuMap = affectedCrops.stream()
                    .filter(c -> c.getMeasureUnit() != null)
                    .collect(Collectors.toMap(Crop::getId, c -> {
                        var mu = c.getMeasureUnit();
                        return (mu.getAbbreviation() != null && !mu.getAbbreviation().isBlank())
                                ? mu.getAbbreviation() : mu.getName();
                    }));
            List<HarvestRecord> affected = harvestRecordRepository.findByFarmerIdAndCrop_IdIn(farmerId, cropIds);
            for (HarvestRecord record : affected) {
                if (record.getCrop() != null) {
                    Long cropId = record.getCrop().getId();
                    record.setArchivedCropName(cropNameMap.getOrDefault(cropId, record.getCrop().getName()));
                    record.setArchivedMeasureUnitName(cropMuMap.get(cropId));
                    record.setCrop(null);
                    record.setArchived(true);
                }
            }
            harvestRecordRepository.saveAll(affected);
            harvestRecordRepository.flush();
            cropRepository.deleteByIdInAndFarmerId(cropIds, farmerId);
        }

        repository.deleteByIdInAndFarmerId(ids, farmerId);
    }

    @Override
    @Transactional
    public void updateActiveBatch(List<Long> ids, Long farmerId, boolean active) {
        if (ids == null || ids.isEmpty()) return;
        repository.updateActiveStatusByIdInAndFarmerId(ids, active, farmerId);
    }

    private MeasureUnit toEntity(MeasureUnitRequest request, Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));

        MeasureUnit unit = new MeasureUnit();
        unit.setName(request.name());
        unit.setAbbreviation(request.abbreviation());
        unit.setFarmer(farmer);
        unit.setActive(request.active() != null ? request.active() : true);
        return unit;
    }

    private MeasureUnitResponse toResponse(MeasureUnit unit) {
        return new MeasureUnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getAbbreviation(),
                unit.isActive()
        );
    }
}