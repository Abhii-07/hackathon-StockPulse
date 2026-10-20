package com.stockpulse.controller;

import com.stockpulse.dto.OrderRequestDTO;
import com.stockpulse.dto.ProductCreateRequestDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.StockUpdateDTO;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.ProductStatus;
import com.stockpulse.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequestDTO productCreateRequestDTO) {
        ProductDTO createdProduct = productService.createProduct(productCreateRequestDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        
        List<ProductDTO> products;
        
        if (status != null && category != null) {
            products = productService.getProductsByStatusAndCategory(
                ProductStatus.valueOf(status.toUpperCase()), 
                Category.valueOf(category.toUpperCase())
            );
        } else if (status != null) {
            products = productService.getProductsByStatus(ProductStatus.valueOf(status.toUpperCase()));
        } else if (category != null) {
            products = productService.getProductsByCategory(Category.valueOf(category.toUpperCase()));
        } else {
            products = productService.getAllProducts();
        }
        
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(@PathVariable Long id, @RequestBody StockUpdateDTO stockUpdateDTO) {
        ProductDTO updatedProduct = productService.updateStockLevel(id, stockUpdateDTO.getStockLevel());
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<ProductDTO> processOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            ProductDTO updatedProduct = productService.processOrder(id, orderRequestDTO.getQuantity());
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}