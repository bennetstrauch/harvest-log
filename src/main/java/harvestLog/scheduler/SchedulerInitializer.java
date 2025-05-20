package harvestLog.scheduler;

import harvestLog.service.impl.HarvestReportService;
import org.springframework.stereotype.Component;

@Component
public class SchedulerInitializer {
    public SchedulerInitializer(HarvestReportService reportService) {
    }
}
