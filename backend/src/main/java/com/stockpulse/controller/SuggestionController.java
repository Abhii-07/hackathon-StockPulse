package com.stockpulse.controller;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SuggestionController {
    private final SuggestionService service;

    @PostMapping("/api/products/{id}/suggest-pricing")
    public Map<String,Object> pricing(@PathVariable Long id) { return priceView(service.generatePricingSuggestion(id)); }
    @PostMapping("/api/products/{id}/suggest-reorder")
    public Map<String,Object> reorder(@PathVariable Long id) { return reorderView(service.generateReorderSuggestion(id)); }
    @GetMapping("/api/pricing-suggestions")
    public List<Map<String,Object>> pendingPricing(@RequestParam(defaultValue="PENDING") SuggestionStatus status) {
        if (status != SuggestionStatus.PENDING) throw new IllegalArgumentException("Only PENDING is supported");
        return service.getPendingPricingSuggestions().stream().map(this::priceView).toList();
    }
    @GetMapping("/api/reorder-suggestions")
    public List<Map<String,Object>> pendingReorder(@RequestParam(defaultValue="PENDING") SuggestionStatus status) {
        if (status != SuggestionStatus.PENDING) throw new IllegalArgumentException("Only PENDING is supported");
        return service.getPendingReorderSuggestions().stream().map(this::reorderView).toList();
    }
    @PatchMapping("/api/pricing-suggestions/{id}")
    public Map<String,Object> approvePricing(@PathVariable Long id, @RequestBody Map<String,SuggestionStatus> body) {
        return priceView(service.updatePricingSuggestionStatus(id,body.get("status")));
    }
    @PatchMapping("/api/reorder-suggestions/{id}")
    public Map<String,Object> approveReorder(@PathVariable Long id, @RequestBody Map<String,SuggestionStatus> body) {
        return reorderView(service.updateReorderSuggestionStatus(id,body.get("status")));
    }
    private Map<String,Object> priceView(PricingSuggestion s) {
        return Map.of("id",s.getId(),"productId",s.getProduct().getId(),
            "currentPrice",s.getCurrentPrice(),"recommendedPrice",s.getRecommendedPrice(),
            "direction",s.getDirection(),"confidence",s.getConfidence(),
            "reasoning",s.getReasoning()==null?"":s.getReasoning(),
            "triggerReason",s.getTriggerReason(),"status",s.getStatus());
    }
    private Map<String,Object> reorderView(ReorderSuggestion s) {
        return Map.of("id",s.getId(),"productId",s.getProduct().getId(),
            "currentStock",s.getCurrentStock(),"recommendedQuantity",s.getRecommendedQuantity(),
            "leadTimeDays",s.getLeadTimeDays(),"confidence",s.getConfidence(),
            "reasoning",s.getReasoning()==null?"":s.getReasoning(),
            "triggerReason",s.getTriggerReason(),"status",s.getStatus());
    }
}
