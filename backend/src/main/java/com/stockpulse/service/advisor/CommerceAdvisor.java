package com.stockpulse.service.advisor;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.TriggerReason;

public interface CommerceAdvisor {
    PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason);
    ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason);
}