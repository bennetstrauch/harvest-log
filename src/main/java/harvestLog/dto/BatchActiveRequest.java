package harvestLog.dto;

import java.util.List;

public record BatchActiveRequest(List<Long> ids, boolean active) {}
