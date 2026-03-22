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

    // Active filtering
    List<Crop> findByFarmerIdAndActive(Long farmerId, boolean active);

    long countByFarmerIdAndActiveTrue(Long farmerId);

    // Soft deletion
    @Modifying
    @Query("UPDATE Crop c SET c.active = false WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int softDeleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Toggle active status
    @Modifying
    @Query("UPDATE Crop c SET c.active = :active WHERE c.id = :id AND c.farmer.id = :farmerId")
    int updateActiveStatusByIdAndFarmerId(@Param("id") Long id, @Param("active") boolean active, @Param("farmerId") Long farmerId);

    // Keep original hard deletion for now (can be removed later)
    @Modifying
    @Query("DELETE FROM Crop c WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int deleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Find crops by measure unit ids (for cascade delete)
    List<Crop> findByMeasureUnit_IdInAndFarmerId(List<Long> unitIds, Long farmerId);

    // Find crops by category ids (for cascade delete)
    List<Crop> findByCategory_IdInAndFarmerId(List<Long> catIds, Long farmerId);

    // Batch active toggle
    @Modifying
    @Query("UPDATE Crop c SET c.active = :active WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int updateActiveStatusByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("active") boolean active, @Param("farmerId") Long farmerId);
}
