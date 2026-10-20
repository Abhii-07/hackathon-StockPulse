package com.stockpulse.service;

import com.stockpulse.dto.ProductCreateRequestDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.ProductStatus;
import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductCreateRequestDTO productCreateRequestDTO);
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByStatus(ProductStatus status);
    List<ProductDTO> getProductsByCategory(Category category);
    List<ProductDTO> getProductsByStatusAndCategory(ProductStatus status, Category category);
    ProductDTO updateStockLevel(Long productId, Integer stockLevel);
    ProductDTO processOrder(Long productId, Integer quantity);
}