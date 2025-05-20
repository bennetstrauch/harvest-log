package harvestLog.repository;

import harvestLog.model.Category;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByFarmer(Farmer farmer);
    List<Crop> findCropsByCategory(Category category);
    // use for query like %name%
    List<Crop> findByNameContainingIgnoreCase(String name);

}
