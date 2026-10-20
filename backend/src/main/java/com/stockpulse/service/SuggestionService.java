package com.stockpulse.service;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;

public interface SuggestionService {
    PricingSuggestion generatePricingSuggestion(Long productId);
    ReorderSuggestion generateReorderSuggestion(Long productId);
}