package com.stockpulse.repository;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion,Long> {
    boolean existsByProductIdAndTriggerReasonAndStatus(Long productId, TriggerReason reason, SuggestionStatus status);
    List<PricingSuggestion> findByStatus(SuggestionStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PricingSuggestion s where s.id = :id")
    Optional<PricingSuggestion> findLockedById(@Param("id") Long id);
}
