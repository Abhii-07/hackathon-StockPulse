
package com.stockpulse.service.advisor;

import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.PriceDirection;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBasedCommerceAdvisor implements CommerceAdvisor {

    private final ProductRepository productRepository;

    @Override
    public PricingSuggestion generatePricingSuggestion(
            Product product, TriggerReason triggerReason) {

        log.info("Generating rule-based pricing suggestion for SKU: {}",
                product.getSku());

        BigDecimal currentPrice = product.getCurrentPrice();
        BigDecimal recommendedPrice = currentPrice;
        PriceDirection direction = PriceDirection.HOLD;
        double confidence = 0.7;
        String reasoning = "No pricing rule triggered; holding current price";

        if (product.getStockLevel() < product.getReorderThreshold()) {
            recommendedPrice = currentPrice
                    .multiply(new BigDecimal("1.10"))
                    .setScale(2, RoundingMode.HALF_UP);

            direction = PriceDirection.INCREASE;
            confidence = 0.9;
            reasoning = "Stock is below reorder threshold; applying 10% increase";

        } else {
            double categoryAverage =
                    calculateCategoryAverageDemand(product.getCategory());

            if (categoryAverage > 0
                    && product.getDemandVelocity() > 2 * categoryAverage) {

                recommendedPrice = currentPrice
                        .multiply(new BigDecimal("1.05"))
                        .setScale(2, RoundingMode.HALF_UP);

                direction = PriceDirection.INCREASE;
                confidence = 0.8;
                reasoning = "Demand exceeds 2x category average; applying 5% increase";
            }
        }

        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentPrice(currentPrice);
        suggestion.setRecommendedPrice(recommendedPrice);
        suggestion.setDirection(direction);
        suggestion.setConfidence(confidence);
        suggestion.setReasoning(reasoning);
        suggestion.setTriggerReason(triggerReason);
        suggestion.setStatus(SuggestionStatus.PENDING);

        log.info("Pricing suggestion for {}: {} -> {}, direction={}",
                product.getSku(), currentPrice, recommendedPrice, direction);

        return suggestion;
    }

    @Override
    public ReorderSuggestion generateReorderSuggestion(
            Product product, TriggerReason triggerReason) {

        log.info("Generating rule-based reorder suggestion for SKU: {}",
                product.getSku());

        int quantity = Math.max(
                1,
                product.getReorderThreshold() * 3 - product.getStockLevel()
        );

        int threshold = product.getReorderThreshold();
        int deficit = threshold - product.getStockLevel();

        double confidence = 0.3;

        if (deficit > 0 && threshold > 0) {
            double ratio = (double) deficit / threshold;
            confidence = Math.min(0.9, 0.5 + ratio * 0.4);
        }

        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentStock(product.getStockLevel());
        suggestion.setRecommendedQuantity(quantity);
        suggestion.setLeadTimeDays(5);
        suggestion.setConfidence(confidence);
        suggestion.setReasoning(
                "Reorder quantity = max(1, threshold * 3 - current stock) = "
                        + quantity
        );
        suggestion.setTriggerReason(triggerReason);
        suggestion.setStatus(SuggestionStatus.PENDING);

        log.info("Reorder suggestion for {}: quantity={}",
                product.getSku(), quantity);

        return suggestion;
    }

    private double calculateCategoryAverageDemand(Category category) {

        List<Product> products = productRepository.findAll()
                .stream()
                .filter(p -> p.getCategory() == category)
                .toList();

        if (products.isEmpty()) {
            return 0.0;
        }

        return products.stream()
                .mapToDouble(Product::getDemandVelocity)
                .average()
                .orElse(0.0);
    }
}
