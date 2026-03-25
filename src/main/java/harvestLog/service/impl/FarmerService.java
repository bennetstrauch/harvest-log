package harvestLog.service.impl;

import harvestLog.dto.FarmerBasicResponse;
import harvestLog.dto.FarmerDetailResponse;
import harvestLog.dto.FarmerRequest;
import harvestLog.exception.EntityNotFoundException;
import harvestLog.model.Farmer;
import harvestLog.repository.FarmerRepository;
import harvestLog.service.IFarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import harvestLog.repository.CropRepository;
import harvestLog.repository.FieldRepository;
import harvestLog.repository.MeasureUnitRepository;
import harvestLog.plan.PlanLimits;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmerService implements IFarmerService, UserDetailsService {
    @Autowired
    private FarmerRepository farmerRepository;
    @Autowired
    private CropRepository cropRepository;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private MeasureUnitRepository measureUnitRepository;

    @Override
    public List<FarmerBasicResponse> getAllFarmers() {
        return farmerRepository.findAll().stream()
                .map(f -> new FarmerBasicResponse(f.getId(), f.getName(), f.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public FarmerDetailResponse getMyProfile(String email) {
        Farmer farmer = findByEmail(email);
        return toResponseDTO(farmer);
    }
    @Override
    public FarmerDetailResponse updateMyProfile(String email, FarmerRequest request) {
        Farmer exsitingFarmer = findByEmail(email);
        exsitingFarmer.setName(request.name());
        exsitingFarmer.setEmail(request.email());
        exsitingFarmer.setPassword(request.password());

        Farmer updated = farmerRepository.save(exsitingFarmer);
        return toResponseDTO(updated);
    }

    @Override
    public void  deleteMyAccount(String email) {
        Farmer farmer = findByEmail(email);
        farmerRepository.deleteById(farmer.getId());
    }

    @Override
    public boolean existsByFarmerEmail(String email) {
        return farmerRepository.existsFarmerByEmail(email);
    }
    @Override
    public Farmer findByEmail(String email) {
        return farmerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found with email: " + email));
    }

    @Override
    public Farmer findById(Long id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found with id: " + id));
    }

    @Override
    public FarmerBasicResponse updatePlan(String targetEmail, harvestLog.model.PlanType planType) {
        Farmer farmer = findByEmail(targetEmail);
        farmer.setPlanType(planType);
        if (planType == harvestLog.model.PlanType.FARM) {
            farmer.setTrialEndsAt(null);
            farmer.setGracePeriodStartedAt(null);
        } else if (planType == harvestLog.model.PlanType.FREE) {
            Long farmerId = farmer.getId();
            boolean hasOverage = cropRepository.countByFarmerIdAndActiveTrue(farmerId) > PlanLimits.FREE_MAX_CROPS
                    || fieldRepository.countByFarmerIdAndActiveTrue(farmerId) > PlanLimits.FREE_MAX_FIELDS
                    || measureUnitRepository.countByFarmerIdAndActiveTrue(farmerId) > PlanLimits.FREE_MAX_MEASURE_UNITS;
            if (hasOverage && farmer.getGracePeriodStartedAt() == null) {
                farmer.setGracePeriodStartedAt(LocalDateTime.now());
            }
        }
        Farmer saved = farmerRepository.save(farmer);
        return new FarmerBasicResponse(saved.getId(), saved.getName(), saved.getEmail());
    }

    @Override
    public FarmerBasicResponse updateMyName(String email, String name) {
        Farmer farmer = findByEmail(email);
        farmer.setName(name);
        Farmer saved = farmerRepository.save(farmer);
        return new FarmerBasicResponse(saved.getId(), saved.getName(), saved.getEmail());
    }

    @Override
    public Farmer create(Farmer farmer) {
        //farmer.setId(null);
        return farmerRepository.save(farmer);
    }

    @Override
    public Farmer update(Long id, Farmer farmerToUpdated) {
        Farmer existing = farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found with ID: " + id));
        existing.setName(farmerToUpdated.getName());
        existing.setEmail(farmerToUpdated.getEmail());
        existing.setPassword(farmerToUpdated.getPassword());

        return farmerRepository.save(existing);
    }

    @Override
    public void deleteById(long id) {
        if (!farmerRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot delete Farmer not found with Id: " + id);
        }
        farmerRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Farmer farmer = farmerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Farmer not found"));
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(farmer.getEmail())
                .password(farmer.getPassword())
                .authorities("FARMER")
                .build();
        return userDetails;
    }

    private FarmerDetailResponse toResponseDTO(Farmer farmer) {
        return new FarmerDetailResponse(
                farmer.getId(),
                farmer.getName(),
                farmer.getEmail(),
                farmer.getHarvestRecords().stream().map(hr -> hr.getId()).toList(),
                farmer.getCrops().stream().map(c -> c.getId()).toList(),
                farmer.getFields().stream().map(f -> f.getId()).toList()
        );
    }

}
