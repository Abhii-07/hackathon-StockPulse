package com.stockpulse;

import com.stockpulse.service.advisor.CommerceAdvisor;
import com.stockpulse.service.advisor.RuleBasedCommerceAdvisor;
import com.stockpulse.service.advisor.GeminiCommerceAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvisorConfig {

    @Value("${llm.provider:rule}")
    private String llmProvider;

    @Bean
    public CommerceAdvisor commerceAdvisor(
            RuleBasedCommerceAdvisor ruleBasedAdvisor,
            GeminiCommerceAdvisor geminiAdvisor) {
        
        if ("gemini".equalsIgnoreCase(llmProvider)) {
            return geminiAdvisor;
        }
        return ruleBasedAdvisor;
    }
}