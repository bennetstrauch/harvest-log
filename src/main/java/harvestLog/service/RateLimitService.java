package harvestLog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private final int perMinuteLimit;
    private final int perDayLimit;
    private final Supplier<Instant> clock;

    private final ConcurrentHashMap<Long, Deque<Instant>> minuteWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> dailyCounters = new ConcurrentHashMap<>();

    @Autowired
    public RateLimitService(
            @Value("${app.rate-limit.per-minute:10}") int perMinuteLimit,
            @Value("${app.rate-limit.per-day:100}") int perDayLimit) {
        this(perMinuteLimit, perDayLimit, Instant::now);
    }

    RateLimitService(int perMinuteLimit, int perDayLimit, Supplier<Instant> clock) {
        this.perMinuteLimit = perMinuteLimit;
        this.perDayLimit = perDayLimit;
        this.clock = clock;
    }

    public RateLimitResult check(Long farmerId) {
        Instant now = clock.get();

        AtomicInteger daily = dailyCounters.computeIfAbsent(farmerId, k -> new AtomicInteger(0));
        if (daily.get() >= perDayLimit) {
            Instant midnight = LocalDate.now(ZoneId.systemDefault())
                    .plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            return new RateLimitResult(false, "per_day", midnight);
        }

        Deque<Instant> window = minuteWindows.computeIfAbsent(farmerId, k -> new ArrayDeque<>());
        synchronized (window) {
            Instant cutoff = now.minusSeconds(60);
            while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
                window.pollFirst();
            }
            if (window.size() >= perMinuteLimit) {
                Instant resetAt = window.peekFirst().plusSeconds(60);
                return new RateLimitResult(false, "per_minute", resetAt);
            }
            window.addLast(now);
        }

        daily.incrementAndGet();
        return RateLimitResult.ok();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCounters() {
        dailyCounters.clear();
    }
}
