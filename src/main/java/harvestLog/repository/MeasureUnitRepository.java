package harvestLog.repository;

import harvestLog.model.MeasureUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeasureUnitRepository extends JpaRepository<MeasureUnit, Long> {
    Optional<MeasureUnit> findByNameIgnoreCase(String name);

    List<MeasureUnit> findAllByFarmer_Id(Long farmerId);

    // Active filtering
    List<MeasureUnit> findByFarmerIdAndActive(Long farmerId, boolean active);

    long countByFarmerIdAndActiveTrue(Long farmerId);

    // For duplicate checking (includes inactive)
    Optional<MeasureUnit> findByNameIgnoreCaseAndFarmerId(String name, Long farmerId);

    // Soft deletion
    @Modifying
    @Query("UPDATE MeasureUnit m SET m.active = false WHERE m.id IN :ids AND m.farmer.id = :farmerId")
    int softDeleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Toggle active status
    @Modifying
    @Query("UPDATE MeasureUnit m SET m.active = :active WHERE m.id = :id AND m.farmer.id = :farmerId")
    int updateActiveStatusByIdAndFarmerId(@Param("id") Long id, @Param("active") boolean active, @Param("farmerId") Long farmerId);

    // Keep original hard deletion for now (can be removed later)
    @Modifying
    @Query("DELETE FROM MeasureUnit m WHERE m.id IN :ids AND m.farmer.id = :farmerId")
    int deleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Batch active toggle
    @Modifying
    @Query("UPDATE MeasureUnit m SET m.active = :active WHERE m.id IN :ids AND m.farmer.id = :farmerId")
    int updateActiveStatusByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("active") boolean active, @Param("farmerId") Long farmerId);
}
