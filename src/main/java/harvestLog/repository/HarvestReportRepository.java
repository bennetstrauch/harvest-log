package harvestLog.repository;

import harvestLog.model.HarvestReport;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HarvestReportRepository extends JpaRepository<HarvestReport, Long> {

}
