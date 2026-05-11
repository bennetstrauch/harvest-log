package harvestLog.service;

import java.time.Instant;

public record RateLimitResult(boolean allowed, String reason, Instant resetAt) {
    public static RateLimitResult ok() {
        return new RateLimitResult(true, null, null);
    }
}
