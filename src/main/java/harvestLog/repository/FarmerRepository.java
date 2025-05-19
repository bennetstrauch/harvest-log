package harvestLog.repository;

import harvestLog.model.Farmer;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    List<Farmer> findByNameContainingIgnoreCase(String name);

    Optional<Farmer> findByEmail(String email);

     boolean existsFarmerByEmail(String email);
}
