package harvestLog.repository;

import harvestLog.model.Category;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByFarmer(Farmer farmer);
    List<Crop> findCropsByCategoryName(String categoryName);
    // use for query like %name%
    Optional<List<Crop>> findByNameContainingIgnoreCase(String name);
    // just deleted, now added
    Optional<Crop> findCropByNameContainingIgnoreCase(String cropName);
}
