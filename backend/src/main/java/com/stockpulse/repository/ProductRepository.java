package com.stockpulse.repository;

import com.stockpulse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findWithPessimisticLockById(Long id);
}