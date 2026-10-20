package com.stockpulse.repository;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    List<PricingSuggestion> findByProductAndStatusOrderByidDesc(Product product, SuggestionStatus status);
    
    Optional<PricingSuggestion> findByProductIdAndTriggerReasonAndStatus(
        Long productId, TriggerReason triggerReason, SuggestionStatus status);
}