package harvestLog.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class EmailCooldownService {

    private static final List<Long> COOLDOWN_SECONDS = List.of(60L, 180L, 300L);

    private final Supplier<Instant> clock;
    private final ConcurrentHashMap<String, Deque<Instant>> history = new ConcurrentHashMap<>();

    public EmailCooldownService() {
        this(Instant::now);
    }

    EmailCooldownService(Supplier<Instant> clock) {
        this.clock = clock;
    }

    public EmailCooldownResult check(String email, String type) {
        String key = type + ":" + email;
        Deque<Instant> sends = history.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (sends) {
            if (sends.isEmpty()) {
                sends.addLast(clock.get());
                return EmailCooldownResult.ok();
            }

            Instant lastSend = sends.peekLast();
            long cooldown = cooldownSeconds(sends.size());
            Instant retryAfter = lastSend.plusSeconds(cooldown);

            if (clock.get().isBefore(retryAfter)) {
                return new EmailCooldownResult(false, retryAfter);
            }

            sends.addLast(clock.get());
            return EmailCooldownResult.ok();
        }
    }

    public void clear(String email) {
        history.remove("resend:" + email);
        history.remove("forgot:" + email);
    }

    private long cooldownSeconds(int sendCount) {
        int index = Math.min(sendCount - 1, COOLDOWN_SECONDS.size() - 1);
        return COOLDOWN_SECONDS.get(index);
    }
}
