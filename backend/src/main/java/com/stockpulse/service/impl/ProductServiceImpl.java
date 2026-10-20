package com.stockpulse.service.impl;

import com.stockpulse.dto.ProductCreateRequestDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.enums.Category;
import com.stockpulse.entity.enums.ProductStatus;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductCreateRequestDTO productCreateRequestDTO) {
        log.info("Creating product with SKU: {}", productCreateRequestDTO.getSku());
        
        Product product = new Product();
        product.setSku(productCreateRequestDTO.getSku());
        product.setName(productCreateRequestDTO.getName());
        product.setCategory(Category.valueOf(productCreateRequestDTO.getCategory().toUpperCase()));
        product.setCurrentPrice(productCreateRequestDTO.getCurrentPrice());
        product.setStockLevel(productCreateRequestDTO.getStockLevel());
        product.setReorderThreshold(productCreateRequestDTO.getReorderThreshold());
        product.setDemandVelocity(productCreateRequestDTO.getDemandVelocity());
        
        // Set initial status based on stock level
        if (product.getStockLevel() <= 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            log.info("Product {} created with OUT_OF_STOCK status due to zero stock", product.getSku());
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateStockLevel(Long productId, Integer stockLevel) {
        log.info("Updating stock level for product ID: {} to {}", productId, stockLevel);
        
        if (stockLevel < 0) {
            log.warn("Attempt to set negative stock level for product ID: {}", productId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock level cannot be negative");
        }
        
        Product product = productRepository.findWithPessimisticLockById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId);
                });
        
        product.setStockLevel(stockLevel);
        
        if (stockLevel <= 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            log.info("Product {} status changed to OUT_OF_STOCK due to zero stock", product.getSku());
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
            log.info("Product {} status changed to ACTIVE due to restocking", product.getSku());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated stock level for product {}: {}", updatedProduct.getSku(), updatedProduct.getStockLevel());
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductDTO processOrder(Long productId, Integer quantity) {
        log.info("Processing order for product ID: {} with quantity: {}", productId, quantity);
        
        if (quantity <= 0) {
            log.warn("Invalid order quantity: {} for product ID: {}", quantity, productId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order quantity must be positive");
        }
        
        Product product = productRepository.findWithPessimisticLockById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId);
                });
        
        if (product.getStockLevel() < quantity) {
            log.warn("Insufficient stock for product {}: requested {}, available {}", 
                    product.getSku(), quantity, product.getStockLevel());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Insufficient stock for product: " + product.getSku() + 
                    ". Available: " + product.getStockLevel() + ", Requested: " + quantity);
        }
        
        // Update stock level
        product.setStockLevel(product.getStockLevel() - quantity);
        
        // Increase demand velocity (simple implementation)
        product.setDemandVelocity(product.getDemandVelocity() + (quantity * 0.1));
        
        // Update status if out of stock
        if (product.getStockLevel() <= 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
            log.info("Product {} status changed to OUT_OF_STOCK after order processing", product.getSku());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Successfully processed order for product {}: remaining stock {}", 
                updatedProduct.getSku(), updatedProduct.getStockLevel());
        return convertToDTO(updatedProduct);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByStatus(ProductStatus status) {
        return productRepository.findAll().stream()
                .filter(product -> product.getStatus() == status)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Category category) {
        return productRepository.findAll().stream()
                .filter(product -> product.getCategory() == category)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByStatusAndCategory(ProductStatus status, Category category) {
        return productRepository.findAll().stream()
                .filter(product -> product.getStatus() == status && product.getCategory() == category)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setCurrentPrice(product.getCurrentPrice());
        dto.setStockLevel(product.getStockLevel());
        dto.setReorderThreshold(product.getReorderThreshold());
        dto.setDemandVelocity(product.getDemandVelocity());
        dto.setStatus(product.getStatus());
        return dto;
    }
}