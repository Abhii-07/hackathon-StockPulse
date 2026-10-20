package com.stockpulse.repository;

import com.stockpulse.entity.InventorySnapshot;
import com.stockpulse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {
    List<InventorySnapshot> findByProductOrderByTimestampDesc(Product product);
}