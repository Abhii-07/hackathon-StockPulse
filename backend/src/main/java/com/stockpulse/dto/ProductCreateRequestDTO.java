package com.stockpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDTO {
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Category is required")
    private String category;
    
    @NotNull(message = "Current price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal currentPrice;
    
    @NotNull(message = "Stock level is required")
    private Integer stockLevel;
    
    @NotNull(message = "Reorder threshold is required")
    private Integer reorderThreshold;
    
    @NotNull(message = "Demand velocity is required")
    private Double demandVelocity;
}