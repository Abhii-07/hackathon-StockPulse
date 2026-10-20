package com.stockpulse;

import com.stockpulse.service.advisor.CommerceAdvisor;
import com.stockpulse.service.advisor.GeminiCommerceAdvisor;
import com.stockpulse.service.advisor.RuleBasedCommerceAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AdvisorConfig {
    @Bean
    @Primary
    public AdvisorRouter advisorRouter(RuleBasedCommerceAdvisor rules, GeminiCommerceAdvisor ai) {
        return new AdvisorRouter(rules, ai);
    }
}
