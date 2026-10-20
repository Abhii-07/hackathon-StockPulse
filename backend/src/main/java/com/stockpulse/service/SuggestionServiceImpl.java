package com.stockpulse.service;

import com.stockpulse.entity.*;
import com.stockpulse.entity.enums.*;
import com.stockpulse.repository.*;
import com.stockpulse.service.advisor.CommerceAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {
    private final ProductRepository products;
    private final PricingSuggestionRepository pricing;
    private final ReorderSuggestionRepository reorders;
    private final CommerceAdvisor advisor;

    @Override @Transactional
    public PricingSuggestion generatePricingSuggestion(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (pricing.existsByProductIdAndTriggerReasonAndStatus(id, TriggerReason.MANUAL, SuggestionStatus.PENDING))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manual pricing suggestion already pending");
        return pricing.save(advisor.generatePricingSuggestion(p, TriggerReason.MANUAL));
    }
    @Override @Transactional
    public ReorderSuggestion generateReorderSuggestion(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (reorders.existsByProductIdAndTriggerReasonAndStatus(id, TriggerReason.MANUAL, SuggestionStatus.PENDING))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manual reorder suggestion already pending");
        return reorders.save(advisor.generateReorderSuggestion(p, TriggerReason.MANUAL));
    }
    private void validate(SuggestionStatus status) {
        if (status == null || status == SuggestionStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose ACCEPTED or REJECTED");
    }
    @Override @Transactional
    public PricingSuggestion updatePricingSuggestionStatus(Long id, SuggestionStatus status) {
        validate(status);
        PricingSuggestion s = pricing.findLockedById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));
        if (s.getStatus() != SuggestionStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion already reviewed");
        if (status == SuggestionStatus.ACCEPTED) {
            Product p = products.findWithPessimisticLockById(s.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            p.setCurrentPrice(s.getRecommendedPrice());
            products.save(p);
        }
        s.setStatus(status);
        return pricing.save(s);
    }
    @Override @Transactional
    public ReorderSuggestion updateReorderSuggestionStatus(Long id, SuggestionStatus status) {
        validate(status);
        ReorderSuggestion s = reorders.findLockedById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));
        if (s.getStatus() != SuggestionStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion already reviewed");
        if (status == SuggestionStatus.ACCEPTED) {
            Product p = products.findWithPessimisticLockById(s.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            p.setStockLevel(Math.addExact(p.getStockLevel(), s.getRecommendedQuantity()));
            if (p.getStockLevel() > 0 && p.getStatus() == ProductStatus.OUT_OF_STOCK)
                p.setStatus(ProductStatus.ACTIVE);
            products.save(p);
        }
        s.setStatus(status);
        return reorders.save(s);
    }
    @Override @Transactional(readOnly=true)
    public List<PricingSuggestion> getPendingPricingSuggestions() { return pricing.findByStatus(SuggestionStatus.PENDING); }
    @Override @Transactional(readOnly=true)
    public List<ReorderSuggestion> getPendingReorderSuggestions() { return reorders.findByStatus(SuggestionStatus.PENDING); }
}
