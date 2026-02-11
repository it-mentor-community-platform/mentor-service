package com.itmentorcommunityplatform.mentorservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter unsuccessfulAttemptsUpdatePriceForGuaranteedReviewCounter(MeterRegistry registry) {
        return Counter.builder("data_unsuccessful_attempts_update_price_for_guaranteed_review_total")
                .description("Total unsuccessful attempts updated for guaranteed review")
                .register(registry);
    }
}
