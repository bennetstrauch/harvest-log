package harvestLog.repository;

import harvestLog.model.Category;
import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByFarmer(Farmer farmer);
    List<Crop> findCropsByCategory(Category category);


    Optional<Crop> findCropByNameContainingIgnoreCase(String cropName);
}
