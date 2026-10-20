package com.stockpulse.entity;

import com.stockpulse.entity.enums.PriceDirection;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal recommendedPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceDirection direction;
    
    @Column(nullable = false)
    private Double confidence;
    
    @Column(length = 1000)
    private String reasoning;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerReason triggerReason;
}