package harvestLog.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    @Test
    void firstMessage_isAllowed() {
        var service = new RateLimitService(10, 100, Instant::now);
        assertThat(service.check(1L).allowed()).isTrue();
    }

    @Test
    void tenthMessage_isAllowed() {
        var service = new RateLimitService(10, 100, Instant::now);
        for (int i = 0; i < 9; i++) service.check(1L);
        assertThat(service.check(1L).allowed()).isTrue();
    }

    @Test
    void eleventhMessage_isDenied_withPerMinuteReason() {
        var service = new RateLimitService(10, 100, Instant::now);
        for (int i = 0; i < 10; i++) service.check(1L);
        var result = service.check(1L);
        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("per_minute");
        assertThat(result.resetAt()).isNotNull();
    }

    @Test
    void afterMinutePasses_perMinuteLimit_resets() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new RateLimitService(10, 100, now::get);
        for (int i = 0; i < 10; i++) service.check(1L);
        now.set(now.get().plusSeconds(61));
        assertThat(service.check(1L).allowed()).isTrue();
    }

    @Test
    void dailyLimit_isDenied_afterExceeding() {
        var service = new RateLimitService(100, 5, Instant::now);
        for (int i = 0; i < 5; i++) service.check(1L);
        var result = service.check(1L);
        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("per_day");
        assertThat(result.resetAt()).isNotNull();
    }

    @Test
    void differentFarmers_haveIndependentLimits() {
        var service = new RateLimitService(3, 100, Instant::now);
        for (int i = 0; i < 3; i++) service.check(1L);
        assertThat(service.check(1L).allowed()).isFalse();
        assertThat(service.check(2L).allowed()).isTrue();
    }

    @Test
    void resetDailyCounters_allowsMessagesAgain() {
        var service = new RateLimitService(100, 3, Instant::now);
        for (int i = 0; i < 3; i++) service.check(1L);
        assertThat(service.check(1L).allowed()).isFalse();
        service.resetDailyCounters();
        assertThat(service.check(1L).allowed()).isTrue();
    }
}
