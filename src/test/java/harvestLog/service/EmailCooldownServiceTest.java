package harvestLog.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EmailCooldownServiceTest {

    @Test
    void firstRequest_isAllowed() {
        var service = new EmailCooldownService(Instant::now);
        assertThat(service.check("a@b.com", "resend").allowed()).isTrue();
    }

    @Test
    void secondRequest_withinOneMinute_isDenied() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(30));
        var result = service.check("a@b.com", "resend");
        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfter()).isEqualTo(Instant.parse("2025-01-01T10:01:00Z"));
    }

    @Test
    void secondRequest_afterOneMinute_isAllowed() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(61));
        assertThat(service.check("a@b.com", "resend").allowed()).isTrue();
    }

    @Test
    void thirdRequest_withinThreeMinutes_isDenied() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(61));
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(60));
        var result = service.check("a@b.com", "resend");
        assertThat(result.allowed()).isFalse();
    }

    @Test
    void thirdRequest_afterThreeMinutes_isAllowed() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(61));
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(181));
        assertThat(service.check("a@b.com", "resend").allowed()).isTrue();
    }

    @Test
    void fourthRequest_capsAtFiveMinutes() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(61));
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(181));
        service.check("a@b.com", "resend");
        now.set(now.get().plusSeconds(301));
        assertThat(service.check("a@b.com", "resend").allowed()).isTrue();
        now.set(now.get().plusSeconds(240));
        assertThat(service.check("a@b.com", "resend").allowed()).isFalse();
    }

    @Test
    void resendAndForgot_areTrackedIndependently() {
        var service = new EmailCooldownService(Instant::now);
        service.check("a@b.com", "resend");
        assertThat(service.check("a@b.com", "forgot").allowed()).isTrue();
    }

    @Test
    void differentEmails_areTrackedIndependently() {
        var service = new EmailCooldownService(Instant::now);
        service.check("a@b.com", "resend");
        assertThat(service.check("c@d.com", "resend").allowed()).isTrue();
    }

    @Test
    void clear_resetsCooldownForBothTypes() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2025-01-01T10:00:00Z"));
        var service = new EmailCooldownService(now::get);
        service.check("a@b.com", "resend");
        service.check("a@b.com", "forgot");
        service.clear("a@b.com");
        assertThat(service.check("a@b.com", "resend").allowed()).isTrue();
        assertThat(service.check("a@b.com", "forgot").allowed()).isTrue();
    }
}
