package harvestLog.service;

import harvestLog.dto.FarmerBasicResponse;
import harvestLog.dto.FarmerDetailResponse;
import harvestLog.dto.FarmerRequest;
import harvestLog.model.Farmer;

import java.util.List;

public interface IFarmerService {
    List<FarmerBasicResponse> getAllFarmers();
    FarmerDetailResponse getMyProfile(String email);
    FarmerDetailResponse updateMyProfile(String email, FarmerRequest request);
    void deleteMyAccount(String email);

    Farmer findByEmail(String email);
    Farmer create(Farmer farmer);
    Farmer update(Long id, Farmer farmer);

    void deleteById(long id);

    boolean existsByFarmerEmail(String email);

}
