package harvestLog.service.impl;

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

import java.util.List;

@Service
public class FarmerService implements IFarmerService, UserDetailsService {
    @Autowired
    private FarmerRepository farmerRepository;

    @Override
    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }

    @Override
    public Farmer findByEmail(String email) {
        return farmerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found with email: " + email));
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
            throw new EntityNotFoundException("Cannot delete.Farmer not found with Id: " + id);
        }
        farmerRepository.deleteById(id);
    }

    @Override
    public List<Farmer> searchByName(String name) {
        return farmerRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public boolean existsByEmail(String email) {
        return farmerRepository.existsFarmerByEmail(email);
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


}
