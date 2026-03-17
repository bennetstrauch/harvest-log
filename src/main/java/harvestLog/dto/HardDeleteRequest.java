package harvestLog.dto;

import java.util.List;

public record HardDeleteRequest(List<Long> ids, boolean cascade) {}
