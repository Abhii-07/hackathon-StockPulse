package com.stockpulse.service.impl;

import com.stockpulse.dto.ProductCreateRequestDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.ProductStatus;
import com.stockpulse.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        // Arrange
        ProductCreateRequestDTO requestDTO = new ProductCreateRequestDTO();
        requestDTO.setSku("TEST123");
        requestDTO.setName("Test Product");
        requestDTO.setCategory("ELECTRONICS");
        requestDTO.setCurrentPrice(new BigDecimal("99.99"));
    @Test
    void updateStockLevel_WithZeroStock_ShouldSetOutOfStockStatus() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST123");
        product.setName("Test Product");
        product.setStockLevel(5);
        product.setStatus(ProductStatus.ACTIVE);

        when(productRepository.findWithPessimisticLockById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductDTO result = productService.updateStockLevel(1L, 0);

        // Assert
        assertEquals(0, result.getStockLevel());
        assertEquals(ProductStatus.OUT_OF_STOCK, result.getStatus());
        verify(productRepository, times(1)).findWithPessimisticLockById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }
        requestDTO.setStockLevel(100);
        requestDTO.setReorderThreshold(20);
        requestDTO.setDemandVelocity(1.5);

        Product product = new Product();
        product.setId(1L);
        product.setSku("TEST123");
        product.setName("Test Product");
        product.setCategory(Category.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("99.99"));
        product.setStockLevel(100);
        product.setReorderThreshold(20);
        product.setDemandVelocity(1.5);
        product.setStatus(ProductStatus.ACTIVE);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductDTO result = productService.createProduct(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("TEST123", result.getSku());
        assertEquals("Test Product", result.getName());
        assertEquals(Category.ELECTRONICS, result.getCategory());
        assertEquals(new BigDecimal("99.99"), result.getCurrentPrice());
        assertEquals(100, result.getStockLevel());
        assertEquals(ProductStatus.ACTIVE, result.getStatus());

        verify(productRepository, times(1)).save(any(Product.class));
    }
}