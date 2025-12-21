package com.lattice.core.application.listener;

import com.lattice.core.application.career.CareerService;
import com.lattice.core.application.wealth.WealthService;
import com.lattice.core.domain.DomainType;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.event.LatticeNodeIngestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Listens for generic node ingestion events and dispatches them to domain-specific services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainDispatchListener {

    private final CareerService careerService;
    private final WealthService wealthService;

    @EventListener
    public void onLatticeNodeIngested(LatticeNodeIngestedEvent event) {
        log.info("Received ingestion event for node {} in domain {}", event.getNodeId(), event.getDomain());
        
        try {
            if (event.getDomain() == DomainType.CAREER) {
                log.info("Dispatching to CareerService...");
                careerService.createLog(event.getContent(), CareerType.INBOX_ITEM, event.getTags());
            } else if (event.getDomain() == DomainType.WEALTH) {
                log.info("Dispatching to WealthService...");
                wealthService.ingestExpense(new WealthService.ExpenseCommand(
                        event.getContent(), 
                        "Auto-Categorized", 
                        null, 
                        "CNY", 
                        null, 
                        OffsetDateTime.now(), 
                        "ingestion-dispatcher", 
                        event.getTags(), 
                        null
                ));
            }
        } catch (Exception e) {
            log.error("Failed to dispatch ingestion event for node {}", event.getNodeId(), e);
        }
    }
}
