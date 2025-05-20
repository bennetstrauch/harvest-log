package harvestLog.controller;

import harvestLog.model.HarvestReport;
import harvestLog.service.impl.HarvestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reports")
public class HarvestReportController {

    @Autowired
    private HarvestReportService reportService;

    @GetMapping
    public ResponseEntity<List<HarvestReport>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HarvestReport> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

}
