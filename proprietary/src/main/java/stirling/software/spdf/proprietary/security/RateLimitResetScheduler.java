package stirling.software.spdf.proprietary.security;

import org.springframework.scheduling.annotation.Scheduled;

// @Component@ConditionalOnProperty(name = "premium.enabled", havingValue = "true")
public class RateLimitResetScheduler {

    private final IPRateLimitingFilter rateLimitingFilter;

    public RateLimitResetScheduler(IPRateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Scheduled(cron = "0 0 0 * * MON") // At 00:00 every Monday TODO: configurable
    public void resetRateLimit() {
        rateLimitingFilter.resetRequestCounts();
    }
}
