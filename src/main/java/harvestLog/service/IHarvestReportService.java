package harvestLog.service;

import harvestLog.model.HarvestReport;

import java.util.List;

public interface IHarvestReportService {
    List<HarvestReport> getAllReports();
    HarvestReport getReportById(Long id);
}
