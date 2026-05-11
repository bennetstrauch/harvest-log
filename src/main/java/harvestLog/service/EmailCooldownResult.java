package harvestLog.service;

import java.time.Instant;

public record EmailCooldownResult(boolean allowed, Instant retryAfter) {
    public static EmailCooldownResult ok() {
        return new EmailCooldownResult(true, null);
    }
}
