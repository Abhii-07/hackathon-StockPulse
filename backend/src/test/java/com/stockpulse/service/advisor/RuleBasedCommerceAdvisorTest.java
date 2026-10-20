package com.stockpulse.service.advisor;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.PriceDirection;
import com.stockpulse.entity.enums.TriggerReason;
import com.stockpulse.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleBasedCommerceAdvisorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RuleBasedCommerceAdvisor advisor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generatePricingSuggestion_WhenStockBelowThreshold_ShouldRecommend10PercentIncrease() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST123");
        product.setCurrentPrice(new BigDecimal("100.00"));
        product.setStockLevel(5);
        product.setReorderThreshold(10);
        product.setDemandVelocity(1.0);
        product.setCategory(Category.ELECTRONICS);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        // Act
        PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.INVENTORY_LOW);

        // Assert
        assertNotNull(suggestion);
        assertEquals(new BigDecimal("110.00"), suggestion.getRecommendedPrice());
        assertEquals(PriceDirection.INCREASE, suggestion.getDirection());
        assertEquals(0.9, suggestion.getConfidence(), 0.01);
        assertTrue(suggestion.getReasoning().contains("Stock is below reorder threshold"));
        assertEquals(TriggerReason.INVENTORY_LOW, suggestion.getTriggerReason());
    }

    @Test
    void generatePricingSuggestion_WhenHighDemandVelocity_ShouldRecommend5PercentIncrease() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST456");
        product.setCurrentPrice(new BigDecimal("50.00"));
        product.setStockLevel(15);
        product.setReorderThreshold(10);
        product.setDemandVelocity(11.0); // Changed from 10.0 to 11.0 to ensure it's greater than 2*average
        product.setCategory(Category.APPAREL);

        Product otherProduct1 = new Product();
        otherProduct1.setCategory(Category.APPAREL);
        otherProduct1.setDemandVelocity(2.0);

        Product otherProduct2 = new Product();
        otherProduct2.setCategory(Category.APPAREL);
        otherProduct2.setDemandVelocity(3.0);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, otherProduct1, otherProduct2));

        // Act
        PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.DEMAND_SPIKE);

        // Assert
        assertNotNull(suggestion);
        assertEquals(new BigDecimal("52.50"), suggestion.getRecommendedPrice());
        assertEquals(PriceDirection.INCREASE, suggestion.getDirection());
        assertEquals(0.8, suggestion.getConfidence(), 0.01);
        assertTrue(suggestion.getReasoning().contains("Demand exceeds 2x category average"));
        assertEquals(TriggerReason.DEMAND_SPIKE, suggestion.getTriggerReason());
    }

    @Test
    void generatePricingSuggestion_WhenNoConditionsMet_ShouldHoldPrice() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST123");
        product.setCurrentPrice(new BigDecimal("100.00"));
        product.setStockLevel(20);
        product.setReorderThreshold(10);
        product.setDemandVelocity(3.0);
        product.setCategory(Category.ELECTRONICS);

        Product otherProduct1 = new Product();
        otherProduct1.setCategory(Category.ELECTRONICS);
        otherProduct1.setDemandVelocity(2.0);

        Product otherProduct2 = new Product();
        otherProduct2.setCategory(Category.ELECTRONICS);
        otherProduct2.setDemandVelocity(3.0);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, otherProduct1, otherProduct2));

        // Act
        PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.MANUAL);

        // Assert
        assertNotNull(suggestion);
        assertEquals(new BigDecimal("100.00"), suggestion.getRecommendedPrice());
        assertEquals(PriceDirection.HOLD, suggestion.getDirection());
        assertEquals(0.7, suggestion.getConfidence(), 0.01);
        assertTrue(suggestion.getReasoning().contains("No pricing rule triggered"));
        assertEquals(TriggerReason.MANUAL, suggestion.getTriggerReason());
    }

    @Test
    void generateReorderSuggestion_ShouldCalculateBasedOnRule() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST123");
        product.setStockLevel(5);
        product.setReorderThreshold(10);
        product.setCategory(Category.ELECTRONICS);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        // Act
        ReorderSuggestion suggestion = advisor.generateReorderSuggestion(product, TriggerReason.INVENTORY_LOW);

        // Assert
        assertNotNull(suggestion);
        assertEquals(25, suggestion.getRecommendedQuantity()); // max(1, 10*3 - 5) = max(1, 25) = 25
        assertEquals(5, suggestion.getCurrentStock());
        assertEquals(TriggerReason.INVENTORY_LOW, suggestion.getTriggerReason());
        assertTrue(suggestion.getReasoning().contains("Reorder quantity ="));
    }
}