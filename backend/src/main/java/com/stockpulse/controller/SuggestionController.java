package com.stockpulse.controller;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @PostMapping("/{id}/suggest-pricing")
    public ResponseEntity<PricingSuggestion> suggestPricing(@PathVariable Long id) {
        try {
            PricingSuggestion suggestion = suggestionService.generatePricingSuggestion(id);
            return ResponseEntity.ok(suggestion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/suggest-reorder")
    public ResponseEntity<ReorderSuggestion> suggestReorder(@PathVariable Long id) {
        try {
            ReorderSuggestion suggestion = suggestionService.generateReorderSuggestion(id);
            return ResponseEntity.ok(suggestion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}