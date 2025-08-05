package harvestLog.service;

import harvestLog.model.MeasureUnit;

import java.util.List;

public interface IMeasureUnitService {
    List<MeasureUnit> getAllForFarmerId(long farmerId);

    MeasureUnit getById(Long id);

    MeasureUnit save(MeasureUnit unit);
    void deleteById(Long id);

}
