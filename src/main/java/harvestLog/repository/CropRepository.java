package harvestLog.repository;

import harvestLog.model.Crop;
import harvestLog.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByFarmer(Farmer farmer);
    List<Crop> findCropsByCategoryName(String categoryName);
    // use for query like %name%
    Optional<List<Crop>> findByNameContainingIgnoreCase(String name);
    // just deleted, now added
    Optional<Crop> findCropByNameContainingIgnoreCase(String cropName);

    Optional<Crop> findByNameIgnoreCaseAndFarmerId(String name, Long farmerId);

    Optional<Crop> findByNameContainingIgnoreCaseAndFarmerId(String substring, Long farmerId);

    Optional<Crop> findByCategoryNameIgnoreCaseAndFarmerId(String categoryName, Long farmerId);

    List<Crop> findByFarmerId(Long farmerId);

    @Modifying
    @Query("DELETE FROM Crop c WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int deleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);
}
