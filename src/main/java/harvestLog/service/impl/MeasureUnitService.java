package harvestLog.service.impl;

import harvestLog.exception.EntityNotFoundException;
import harvestLog.model.MeasureUnit;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.service.IMeasureUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeasureUnitService implements IMeasureUnitService {

    @Autowired
    private MeasureUnitRepository repository;


    @Override
    public List<MeasureUnit> getAllForFarmerId(long farmerId) {
        return repository.findAllByFarmer_Id(farmerId);
    }



    @Override
    public MeasureUnit getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Measure Unit not found"));
    }

    @Override
    public MeasureUnit save(MeasureUnit unit) {
        return repository.save(unit);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
