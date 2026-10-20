package com.stockpulse.service;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.TriggerReason;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import com.stockpulse.service.advisor.CommerceAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {

    private final ProductRepository productRepository;
    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;
    private final CommerceAdvisor commerceAdvisor;

    @Override
    public PricingSuggestion generatePricingSuggestion(Long productId) {
        log.info("Generating pricing suggestion for product ID: {}", productId);
        
        // Load the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        
        // Check if a PENDING suggestion already exists for this product and trigger reason
        if (pricingSuggestionRepository.existsByProductIdAndTriggerReasonAndStatus(
                productId, TriggerReason.MANUAL, com.stockpulse.entity.enums.SuggestionStatus.PENDING)) {
            throw new RuntimeException("A pending pricing suggestion already exists for this product");
        }
        
        // Call the advisor to generate the suggestion
        PricingSuggestion suggestion = commerceAdvisor.generatePricingSuggestion(product, TriggerReason.MANUAL);
        
        // Save the generated suggestion
        PricingSuggestion savedSuggestion = pricingSuggestionRepository.save(suggestion);
        
        log.info("Saved pricing suggestion ID: {} for product ID: {}", savedSuggestion.getId(), productId);
        
        return savedSuggestion;
    }

    @Override
    public ReorderSuggestion generateReorderSuggestion(Long productId) {
        log.info("Generating reorder suggestion for product ID: {}", productId);
        
        // Load the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        
        // Check if a PENDING suggestion already exists for this product and trigger reason
        if (reorderSuggestionRepository.existsByProductIdAndTriggerReasonAndStatus(
                productId, TriggerReason.MANUAL, com.stockpulse.entity.enums.SuggestionStatus.PENDING)) {
            throw new RuntimeException("A pending reorder suggestion already exists for this product");
        }
        
        // Call the advisor to generate the suggestion
        ReorderSuggestion suggestion = commerceAdvisor.generateReorderSuggestion(product, TriggerReason.MANUAL);
        
        // Save the generated suggestion
        ReorderSuggestion savedSuggestion = reorderSuggestionRepository.save(suggestion);
        
        log.info("Saved reorder suggestion ID: {} for product ID: {}", savedSuggestion.getId(), productId);
        
        return savedSuggestion;
    }
}