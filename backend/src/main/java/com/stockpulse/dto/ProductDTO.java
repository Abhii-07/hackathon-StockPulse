package com.stockpulse.dto;

import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private Category category;
    private BigDecimal currentPrice;
    private Integer stockLevel;
    private Integer reorderThreshold;
    private Double demandVelocity;
    private ProductStatus status;
}