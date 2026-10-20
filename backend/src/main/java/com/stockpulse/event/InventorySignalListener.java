package com.stockpulse.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySignalListener {
    private final InventorySuggestionGenerator generator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventoryChanged(InventoryChangedEvent event) {
        try { generator.generate(event.productId()); }
        catch (Exception e) {
            log.error("Inventory suggestion generation failed for product {}: {}",
                event.productId(), e.getMessage());
        }
    }
}
