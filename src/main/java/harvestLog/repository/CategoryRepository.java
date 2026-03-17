package harvestLog.repository;

import harvestLog.model.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByFarmerId(long id, Sort name);

    // Active filtering
    List<Category> findByFarmerIdAndActive(Long farmerId, boolean active);

    // For duplicate checking (includes inactive) - already exists
    Optional<Category> findByNameIgnoreCaseAndFarmerId(String normalized, Long farmerId);

    // Soft deletion
    @Modifying
    @Query("UPDATE Category c SET c.active = false WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int softDeleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Toggle active status
    @Modifying
    @Query("UPDATE Category c SET c.active = :active WHERE c.id = :id AND c.farmer.id = :farmerId")
    int updateActiveStatusByIdAndFarmerId(@Param("id") Long id, @Param("active") boolean active, @Param("farmerId") Long farmerId);

    // Keep original hard deletion for now (can be removed later)
    @Modifying
    @Query("DELETE FROM Category c WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int deleteByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("farmerId") Long farmerId);

    // Batch active toggle
    @Modifying
    @Query("UPDATE Category c SET c.active = :active WHERE c.id IN :ids AND c.farmer.id = :farmerId")
    int updateActiveStatusByIdInAndFarmerId(@Param("ids") List<Long> ids, @Param("active") boolean active, @Param("farmerId") Long farmerId);
}
