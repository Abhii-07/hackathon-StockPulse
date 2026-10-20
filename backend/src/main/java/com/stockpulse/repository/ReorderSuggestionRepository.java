package com.stockpulse.repository;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.enums.SuggestionStatus;
import com.stockpulse.entity.enums.TriggerReason;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion,Long> {
    boolean existsByProductIdAndTriggerReasonAndStatus(Long productId, TriggerReason reason, SuggestionStatus status);
    List<ReorderSuggestion> findByStatus(SuggestionStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ReorderSuggestion s where s.id = :id")
    Optional<ReorderSuggestion> findLockedById(@Param("id") Long id);
}
