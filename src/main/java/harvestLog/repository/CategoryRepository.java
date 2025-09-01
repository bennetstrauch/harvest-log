package harvestLog.repository;

import harvestLog.model.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByFarmerId(long id, Sort name);

    Optional<Category> findByNameIgnoreCaseAndFarmerId(String normalized, Long farmerId);
}
