package harvestLog.repository;

import harvestLog.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface HarvestRecordRepo
        extends JpaRepository<HarvestRecord, Long>, JpaSpecificationExecutor<HarvestRecord> {

    List<HarvestRecord> findByFarmerId(Long farmerId);

    // used for GET /api/crops/{id}/harvests
    List<HarvestRecord> findByCrop_Id(Long cropId);
}
