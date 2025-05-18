package harvestLog.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class FarmerIdExtractor {

    public static Long getAuthenticatedFarmerId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Long) {
            return (Long) details;
        }
        throw new IllegalStateException("Farmer ID not found in authentication context");
    }

}
