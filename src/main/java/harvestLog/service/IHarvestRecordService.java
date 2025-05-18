package harvestLog.service;

import harvestLog.model.HarvestRecord;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IHarvestRecordService {

    public List<HarvestRecord> getAllHarvestRecords();

}
