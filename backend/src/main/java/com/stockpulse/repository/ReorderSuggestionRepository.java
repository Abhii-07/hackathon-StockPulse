package com.stockpulse.repository;

import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    List<ReorderSuggestion> findByProductAndStatusOrderByidDesc(Product product, SuggestionStatus status);
    
    Optional<ReorderSuggestion> findByProductIdAndTriggerReasonAndStatus(
        Long productId, TriggerReason triggerReason, SuggestionStatus status);
}