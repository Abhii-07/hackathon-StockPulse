package com.stockpulse.event;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.enums.*;
import com.stockpulse.repository.*;
import com.stockpulse.service.advisor.CommerceAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventorySuggestionGenerator {
    private final ProductRepository products;
    private final PricingSuggestionRepository pricing;
    private final ReorderSuggestionRepository reorders;
    private final CommerceAdvisor advisor;
    @Value("${stockpulse.demand-spike-multiplier:3.0}")
    private double spikeMultiplier;

    @Transactional
    public void generate(Long productId) {
        Product p = products.findWithPessimisticLockById(productId).orElse(null);
        if (p == null) return;
        boolean low = p.getStockLevel() < p.getReorderThreshold();
        double average = products.findAll().stream()
            .filter(other -> other.getCategory() == p.getCategory())
            .mapToDouble(other -> other.getDemandVelocity() == null ? 0 : other.getDemandVelocity())
            .average().orElse(0);
        boolean spike = average > 0 && p.getDemandVelocity() > spikeMultiplier * average;
        if (low) generateFor(p, TriggerReason.INVENTORY_LOW);
        if (spike) generateFor(p, TriggerReason.DEMAND_SPIKE);
    }
    private void generateFor(Product p, TriggerReason reason) {
        Long id = p.getId();
        if (!pricing.existsByProductIdAndTriggerReasonAndStatus(id, reason, SuggestionStatus.PENDING)) {
            try { pricing.save(advisor.generatePricingSuggestion(p, reason)); }
            catch (Exception e) { log.error("Pricing recommendation failed for product {}", id, e); }
        }
        if (!reorders.existsByProductIdAndTriggerReasonAndStatus(id, reason, SuggestionStatus.PENDING)) {
            try { reorders.save(advisor.generateReorderSuggestion(p, reason)); }
            catch (Exception e) { log.error("Reorder recommendation failed for product {}", id, e); }
        }
    }
}
