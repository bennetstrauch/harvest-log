package harvestLog.service.ai;

import harvestLog.dto.MeasureUnitRequest;
import harvestLog.dto.MeasureUnitResponse;
import harvestLog.service.IMeasureUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@Service
public class MeasureUnitAiToolService {

    private static final Logger logger = LoggerFactory.getLogger(MeasureUnitAiToolService.class);

    private final IMeasureUnitService measureUnitService;

    public MeasureUnitAiToolService(IMeasureUnitService measureUnitService) {
        this.measureUnitService = measureUnitService;
    }

    @Tool(description = "Get all active measure units for the current farmer (e.g. kg, tons, liters).")
    public List<MeasureUnitResponse> getAllMeasureUnits() {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Fetching all measure units for farmer {}", farmerId);
        return measureUnitService.getActiveForFarmerId(farmerId);
    }

    @Tool(description = "Create a new measure unit with a name and optional abbreviation (e.g. name='Kilogram', abbreviation='kg').")
    public MeasureUnitResponse createMeasureUnit(String name, String abbreviation) {
        Long farmerId = getAuthenticatedFarmerId();
        logger.info("Creating measure unit '{}' ({}) for farmer {}", name, abbreviation, farmerId);
        return measureUnitService.create(new MeasureUnitRequest(name, abbreviation, true), farmerId);
    }
}
