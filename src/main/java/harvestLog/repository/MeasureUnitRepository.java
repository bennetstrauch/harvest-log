package harvestLog.repository;

import harvestLog.model.MeasureUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeasureUnitRepository extends JpaRepository<MeasureUnit, Long> {
    Optional<MeasureUnit> findByNameIgnoreCase(String name);

    List<MeasureUnit> findAllByFarmer_Id(Long farmerId);
}
