package harvestLog.service;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;
import harvestLog.model.Farmer;
import harvestLog.model.Field;
import harvestLog.repository.FarmerRepository;
import harvestLog.repository.FieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieldService {
    private final FieldRepository fieldRepo;
    private final FarmerRepository farmerRepo;

    public FieldService(FieldRepository fieldRepo, FarmerRepository farmerRepo) {
        this.fieldRepo = fieldRepo;
        this.farmerRepo = farmerRepo;
    }

    public List<FieldResponse> getAllForFarmer(Long farmerId) {
        return fieldRepo.findByFarmerId(farmerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<FieldResponse> getById(Long id, Long farmerId) {
        return fieldRepo.findById(id)
                .filter(field -> field.getFarmer().getId().equals(farmerId))
                .map(this::toResponse);
    }

    @Transactional
    public FieldResponse create(FieldRequest request, Long farmerId) {
        Field field = toEntity(request, farmerId);
        Field saved = fieldRepo.save(field);
        return toResponse(saved);
    }

    @Transactional
    public Optional<FieldResponse> update(Long id, FieldRequest request, Long farmerId) {
        return fieldRepo.findById(id)
                .filter(field -> field.getFarmer().getId().equals(farmerId))
                .map(field -> {
                    field.setName(request.name());
                    return toResponse(fieldRepo.save(field));
                });
    }

    @Transactional
    public boolean delete(Long id, Long farmerId) {
        Optional<Field> field = fieldRepo.findById(id);
        if (field.isPresent() && field.get().getFarmer().getId().equals(farmerId)) {
            fieldRepo.deleteById(id);
            return true;
        }
        return false;
    }

    private Field toEntity(FieldRequest request, Long farmerId) {
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found: " + farmerId));
        Field field = new Field();
        field.setName(request.name());
        field.setFarmer(farmer);
        return field;
    }

    private FieldResponse toResponse(Field field) {
        return new FieldResponse(
                field.getId(),
                field.getName(),
                field.getFarmer().getId()
        );
    }
}