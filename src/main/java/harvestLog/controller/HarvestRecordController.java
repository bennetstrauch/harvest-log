package harvestLog.controller;

import harvestLog.model.HarvestRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/harvest-record")
public class HarvestRecordController {

    @GetMapping()
    public List<HarvestRecord> getHarvestRecords() {

    }
}
