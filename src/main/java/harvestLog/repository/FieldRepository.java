package harvestLog.repository;

import harvestLog.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByFarmerId(Long farmerId);

    Optional<Field> findByNameIgnoreCaseAndFarmerId(String name, Long farmerId);

}