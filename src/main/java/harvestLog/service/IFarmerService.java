package harvestLog.service;

import harvestLog.model.Farmer;

import java.util.List;

public interface IFarmerService {
    List<Farmer> getAllFarmers();
    Farmer findByEmail(String email);
    Farmer create(Farmer farmer);
    Farmer update(Long id, Farmer farmer);

    void deleteById(long id);
    List<Farmer> searchByName(String name);
    boolean existsByEmail(String email);

}
