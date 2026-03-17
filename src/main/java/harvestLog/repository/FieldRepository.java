package harvestLog.repository;

import harvestLog.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByFarmerId(Long farmerId);

    // Active filtering
    List<Field> findByFarmerIdAndActive(Long farmerId, boolean active);

    Optional<Field> findByNameIgnoreCaseAndFarmerId(String name, Long farmerId);

    // Soft deletion
    @Modifying
    @Query("UPDATE Field f SET f.active = false WHERE f.id IN :ids AND f.farmer.id = :farmerId")
    int softDeleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Toggle active status
    @Modifying
    @Query("UPDATE Field f SET f.active = :active WHERE f.id = :id AND f.farmer.id = :farmerId")
    int updateActiveStatusByIdAndFarmerId(@Param("id") Long id, @Param("active") boolean active, @Param("farmerId") Long farmerId);

    // Keep original hard deletion for now (can be removed later)
    @Modifying
    @Query("DELETE FROM Field f WHERE f.id IN :ids AND f.farmer.id = :farmerId")
    int deleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Batch active toggle
    @Modifying
    @Query("UPDATE Field f SET f.active = :active WHERE f.id IN :ids AND f.farmer.id = :farmerId")
    int updateActiveStatusByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("active") boolean active, @Param("farmerId") Long farmerId);
}