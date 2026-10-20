package com.stockpulse.service;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.SuggestionStatus;
import java.util.List;

public interface SuggestionService {
    PricingSuggestion generatePricingSuggestion(Long productId);
    ReorderSuggestion generateReorderSuggestion(Long productId);
    PricingSuggestion updatePricingSuggestionStatus(Long id, SuggestionStatus status);
    ReorderSuggestion updateReorderSuggestionStatus(Long id, SuggestionStatus status);
    List<PricingSuggestion> getPendingPricingSuggestions();
    List<ReorderSuggestion> getPendingReorderSuggestions();
}
