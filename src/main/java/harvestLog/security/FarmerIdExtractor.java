package harvestLog.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class FarmerIdExtractor {
    private static final Logger logger = LoggerFactory.getLogger(FarmerIdExtractor.class);

    public static Long getAuthenticatedFarmerId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Long farmerId) {
            logger.debug("Extracted farmerId: {}", farmerId);
            return farmerId;
        }
        logger.error("Farmer ID not found in authentication context");
        throw new IllegalStateException("Farmer ID not found in authentication context");
    }
}