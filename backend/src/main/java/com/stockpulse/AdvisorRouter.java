package com.stockpulse;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.TriggerReason;
import com.stockpulse.service.advisor.CommerceAdvisor;
import com.stockpulse.service.advisor.GeminiCommerceAdvisor;
import com.stockpulse.service.advisor.RuleBasedCommerceAdvisor;

public class AdvisorRouter implements CommerceAdvisor {
    private final RuleBasedCommerceAdvisor rules;
    private final GeminiCommerceAdvisor ai;
    private volatile String mode = "RULE";

    public AdvisorRouter(RuleBasedCommerceAdvisor rules, GeminiCommerceAdvisor ai) {
        this.rules = rules;
        this.ai = ai;
    }
    public String getMode() { return mode; }
    public void setMode(String next) {
        if (!"RULE".equalsIgnoreCase(next) && !"AI".equalsIgnoreCase(next))
            throw new IllegalArgumentException("mode must be RULE or AI");
        mode = next.toUpperCase(java.util.Locale.ROOT);
    }
    private CommerceAdvisor selected() { return "AI".equals(mode) ? ai : rules; }
    @Override public PricingSuggestion generatePricingSuggestion(Product p, TriggerReason t) {
        return selected().generatePricingSuggestion(p,t);
    }
    @Override public ReorderSuggestion generateReorderSuggestion(Product p, TriggerReason t) {
        return selected().generateReorderSuggestion(p,t);
    }
}
