package harvestLog.service;

import harvestLog.dto.FieldRequest;
import harvestLog.dto.FieldResponse;

import java.util.List;
import java.util.Optional;

public interface IFieldService {

    List<FieldResponse> getAllForFarmer(Long farmerId);

    Optional<FieldResponse> getById(Long id, Long farmerId);

    FieldResponse create(FieldRequest request, Long farmerId);

    Optional<FieldResponse> update(Long id, FieldRequest request, Long farmerId);

    boolean delete(Long id, Long farmerId);
}
