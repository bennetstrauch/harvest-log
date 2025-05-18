package harvestLog.repository;

import harvestLog.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HarvestRecordRepo extends JpaRepository<HarvestRecord, Long> {
}
