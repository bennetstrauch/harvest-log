package harvestLog.repository;

import harvestLog.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HarvestRecordRepository
        extends JpaRepository<HarvestRecord, Long>, JpaSpecificationExecutor<HarvestRecord> {

    List<HarvestRecord> findByFarmerId(Long farmerId);

    // get Latest for Farmer
    Optional<HarvestRecord> findTopByFarmerIdOrderByDateDescIdDesc(Long farmerId);

    // used for GET /api/crops/{id}/harvests
    List<HarvestRecord> findByCrop_Id(Long cropId);

    // use for generated weekly harvest-report
    List<HarvestRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // optimised query for harvest log page — avoids N+1 on farmer + fields
    @Query("SELECT DISTINCT hr FROM HarvestRecord hr " +
           "JOIN FETCH hr.farmer " +
           "LEFT JOIN FETCH hr.fields " +
           "WHERE hr.farmer.id = :farmerId " +
           "AND hr.date >= :startDate AND hr.date <= :endDate")
    List<HarvestRecord> findByFarmerIdAndDateRange(
            @Param("farmerId") Long farmerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // find records by crop ids (for crop cascade archive)
    @Query("SELECT hr FROM HarvestRecord hr WHERE hr.farmer.id = :farmerId AND hr.crop.id IN :cropIds")
    List<HarvestRecord> findByFarmerIdAndCrop_IdIn(@Param("farmerId") Long farmerId, @Param("cropIds") List<Long> cropIds);

    // count records by crop ids
    @Query("SELECT COUNT(hr) FROM HarvestRecord hr WHERE hr.farmer.id = :farmerId AND hr.crop.id IN :cropIds")
    int countByFarmerIdAndCrop_IdIn(@Param("farmerId") Long farmerId, @Param("cropIds") List<Long> cropIds);

    // find records referencing these field ids (for field cascade)
    @Query("SELECT DISTINCT hr FROM HarvestRecord hr JOIN hr.fields f WHERE hr.farmer.id = :farmerId AND f.id IN :fieldIds")
    List<HarvestRecord> findDistinctByFarmerIdAndFieldIds(@Param("farmerId") Long farmerId, @Param("fieldIds") List<Long> fieldIds);

    // count records referencing these field ids
    @Query("SELECT COUNT(DISTINCT hr) FROM HarvestRecord hr JOIN hr.fields f WHERE hr.farmer.id = :farmerId AND f.id IN :fieldIds")
    int countDistinctByFarmerIdAndFieldIds(@Param("farmerId") Long farmerId, @Param("fieldIds") List<Long> fieldIds);
}
