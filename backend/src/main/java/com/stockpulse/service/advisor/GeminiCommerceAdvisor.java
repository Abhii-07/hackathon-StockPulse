package com.stockpulse.service.advisor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.ai.LLMGateway;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.PriceDirection;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiCommerceAdvisor implements CommerceAdvisor {

    private final LLMGateway llmGateway;
    private final RuleBasedCommerceAdvisor ruleBasedAdvisor;
    private final ObjectMapper objectMapper;

    @Override
    public PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason) {
        try {
            String response = llmGateway.generateContent(createPricingPrompt(product, triggerReason));
            return parsePricingResponse(response, product, triggerReason);
        } catch (Exception e) {
            log.warn("Gemini pricing failed for SKU {}. Using rule-based fallback: {}",
                    product.getSku(), e.getMessage());
            return ruleBasedAdvisor.generatePricingSuggestion(product, triggerReason);
        }
    }

    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason) {
        try {
            String response = llmGateway.generateContent(createReorderPrompt(product, triggerReason));
            return parseReorderResponse(response, product, triggerReason);
        } catch (Exception e) {
            log.warn("Gemini reorder failed for SKU {}. Using rule-based fallback: {}",
                    product.getSku(), e.getMessage());
            return ruleBasedAdvisor.generateReorderSuggestion(product, triggerReason);
        }
    }

    private String createPricingPrompt(Product product, TriggerReason triggerReason) {
        String context = productContext(product, triggerReason);
        String situation;
        if (product.getStockLevel() < product.getReorderThreshold()) {
            situation = "INVENTORY LOW: Stock is below the reorder threshold. "
                    + "Consider a modest price increase to protect scarce inventory. "
                    + "The rule-based baseline is a 10% increase.";
        } else if (triggerReason == TriggerReason.DEMAND_SPIKE) {
            situation = "DEMAND SPIKE: Demand has increased unusually quickly. "
                    + "Consider a modest price increase without exploiting customers. "
                    + "The rule-based baseline is a 5% increase when velocity exceeds twice the category average.";
        } else {
            situation = "MANUAL REVIEW: Evaluate whether the price should be held or adjusted. "
                    + "Do not invent demand trends or competitor information.";
        }

        return "You are StockPulse's retail pricing advisor. You only RECOMMEND; "
                + "a human must approve any price change. Use only supplied data. "
                + "Return one raw JSON object, without markdown or explanations outside JSON.\n\n"
                + situation + "\n\n" + context + "\n\n"
                + "Return exactly these fields: "
                + "{\"recommendedPrice\":99.99,\"direction\":\"INCREASE\","
                + "\"confidence\":0.85,\"reasoning\":\"Specific evidence-based reason\"}. "
                + "direction must be INCREASE, DECREASE, or HOLD. "
                + "recommendedPrice must be positive and at most 20% different from currentPrice. "
                + "confidence must be a number from 0 to 1. "
                + "For HOLD, use the current price.";
    }

    private String createReorderPrompt(Product product, TriggerReason triggerReason) {
        int baselineQuantity = Math.max(1, product.getReorderThreshold() * 3 - product.getStockLevel());
        return "You are StockPulse's inventory replenishment advisor. "
                + "You only RECOMMEND; a human must approve any replenishment. "
                + "Use only supplied data. Do not invent supplier contracts or lead times. "
                + "Return one raw JSON object without markdown.\n\n"
                + productContext(product, triggerReason) + "\n"
                + "Rule-based baseline reorder quantity: " + baselineQuantity + ". "
                + "Assume a 5-day lead time unless real supplier data is supplied.\n\n"
                + "Return exactly these fields: "
                + "{\"recommendedQuantity\":25,\"leadTimeDays\":5,"
                + "\"confidence\":0.85,\"reasoning\":\"Specific evidence-based reason\"}. "
                + "recommendedQuantity and leadTimeDays must be positive integers, "
                + "confidence must be a number from 0 to 1.";
    }

    private String productContext(Product product, TriggerReason triggerReason) {
        return "Product information:\n"
                + "SKU: " + product.getSku() + "\n"
                + "Name: " + product.getName() + "\n"
                + "Category: " + product.getCategory() + "\n"
                + "Current price: " + product.getCurrentPrice() + "\n"
                + "Stock level: " + product.getStockLevel() + "\n"
                + "Reorder threshold: " + product.getReorderThreshold() + "\n"
                + "Demand velocity: " + product.getDemandVelocity() + "\n"
                + "Status: " + product.getStatus() + "\n"
                + "Trigger reason: " + triggerReason;
    }

    private PricingSuggestion parsePricingResponse(String response, Product product, TriggerReason triggerReason)
            throws Exception {
        JsonNode root = parseJsonObject(response);
        BigDecimal price = requiredDecimal(root, "recommendedPrice")
                .setScale(2, RoundingMode.HALF_UP);
        PriceDirection direction = PriceDirection.valueOf(requiredText(root, "direction"));
        double confidence = requiredConfidence(root);
        String reasoning = requiredText(root, "reasoning");

        BigDecimal current = product.getCurrentPrice();
        if (current == null || current.signum() <= 0 || price.signum() <= 0) {
            throw new IllegalArgumentException("Prices must be positive");
        }
        BigDecimal lower = current.multiply(new BigDecimal("0.80"));
        BigDecimal upper = current.multiply(new BigDecimal("1.20"));
        if (price.compareTo(lower) < 0 || price.compareTo(upper) > 0) {
            throw new IllegalArgumentException("Price exceeds 20% safety band");
        }
        int comparison = price.compareTo(current);
        if ((comparison > 0 && direction != PriceDirection.INCREASE)
                || (comparison < 0 && direction != PriceDirection.DECREASE)
                || (comparison == 0 && direction != PriceDirection.HOLD)) {
            throw new IllegalArgumentException("Price direction does not match recommended price");
        }

        PricingSuggestion suggestion = new PricingSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentPrice(current);
        suggestion.setRecommendedPrice(price);
        suggestion.setDirection(direction);
        suggestion.setConfidence(confidence);
        suggestion.setReasoning(reasoning);
        suggestion.setTriggerReason(triggerReason);
        suggestion.setStatus(SuggestionStatus.PENDING);
        log.info("Gemini pricing suggestion generated for SKU {}", product.getSku());
        return suggestion;
    }

    private ReorderSuggestion parseReorderResponse(String response, Product product, TriggerReason triggerReason)
            throws Exception {
        JsonNode root = parseJsonObject(response);
        int quantity = requiredPositiveInt(root, "recommendedQuantity");
        int leadTime = requiredPositiveInt(root, "leadTimeDays");
        double confidence = requiredConfidence(root);
        String reasoning = requiredText(root, "reasoning");
        if (quantity > 100_000 || leadTime > 365) {
            throw new IllegalArgumentException("Reorder recommendation exceeds safety limits");
        }

        ReorderSuggestion suggestion = new ReorderSuggestion();
        suggestion.setProduct(product);
        suggestion.setCurrentStock(product.getStockLevel());
        suggestion.setRecommendedQuantity(quantity);
        suggestion.setLeadTimeDays(leadTime);
        suggestion.setConfidence(confidence);
        suggestion.setReasoning(reasoning);
        suggestion.setTriggerReason(triggerReason);
        suggestion.setStatus(SuggestionStatus.PENDING);
        log.info("Gemini reorder suggestion generated for SKU {}", product.getSku());
        return suggestion;
    }

    private JsonNode parseJsonObject(String response) throws Exception {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Gemini returned an empty response");
        }
        String json = response.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "").trim();
        }
        JsonNode root = objectMapper.readTree(json);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Gemini response is not a JSON object");
        }
        return root;
    }

    private String requiredText(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing or invalid field: " + field);
        }
        return value.asText().trim();
    }

    private BigDecimal requiredDecimal(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isNumber()) {
            throw new IllegalArgumentException("Missing or invalid numeric field: " + field);
        }
        return value.decimalValue();
    }

    private int requiredPositiveInt(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() <= 0) {
            throw new IllegalArgumentException("Missing or invalid positive integer: " + field);
        }
        return value.intValue();
    }

    private double requiredConfidence(JsonNode root) {
        JsonNode value = root.get("confidence");
        if (value == null || !value.isNumber()) {
            throw new IllegalArgumentException("Missing or invalid confidence");
        }
        double confidence = value.doubleValue();
        if (!Double.isFinite(confidence) || confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence outside 0..1");
        }
        return confidence;
    }
}
