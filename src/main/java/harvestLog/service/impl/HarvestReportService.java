package harvestLog.service.impl;

import harvestLog.exception.EntityNotFoundException;
import harvestLog.model.HarvestRecord;
import harvestLog.model.HarvestReport;
import harvestLog.repository.HarvestRecordRepo;
import harvestLog.repository.HarvestReportRepository;
import harvestLog.service.IHarvestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HarvestReportService implements IHarvestReportService {

    @Autowired
    private HarvestReportRepository reportRepository;
    @Autowired
    private HarvestRecordRepo recordRepo;

    // this method will be triggered weekly on Sunday at 12:00pm
    // Day of month (* = any day)/Month (* = any month)/Day of week (SUN = Sunday)
   // @Scheduled(cron = "0 * * * * *") --> for test
    @Scheduled(cron = "0 0 12 ? * SUN")
    public void generateWeeklyReport() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.with(DayOfWeek.SUNDAY);
        LocalDate startDate = endDate.minusDays(6);
        List<HarvestRecord> records = recordRepo.findByDateBetween(startDate, endDate);

        Map<String, Double> cropSummary = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCrop().getName(),
                        Collectors.summingDouble(HarvestRecord::getHarvestedQuantity)));
        HarvestReport weeklyReport = new HarvestReport();
        weeklyReport.setStartDate(startDate);
        weeklyReport.setEndDate(endDate);
        weeklyReport.setHarvests(cropSummary);

        reportRepository.save(weeklyReport);
    }

    public List<HarvestReport> getAllReports() {
        return reportRepository.findAll();
    }

    public HarvestReport getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));
    }
}
