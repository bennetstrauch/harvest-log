package harvestLog.repository;

import harvestLog.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
