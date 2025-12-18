package com.lattice.core.application.wealth;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.agent.finance.model.FireflyExpenseCommand;
import com.lattice.core.domain.wealth.WealthEntry;
import com.lattice.core.infrastructure.exception.ValidationException;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.tools.FireflyClient;
import com.lattice.core.infrastructure.tools.PythonWorkerClient;
import com.lattice.core.repository.wealth.WealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Specialist service handling wealth ingestion, budget checks, and Firefly writes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WealthService {

    private final WealthRepository wealthRepository;
    private final PythonWorkerClient pythonWorkerClient;
    private final FireflyClient fireflyClient;
    private final FireflyProperties fireflyProperties;
    private final EmbeddingModel embeddingModel;

    @Transactional
    public WealthEntry ingestExpense(ExpenseCommand command) {
        log.info("[traceId={}] Incoming wealth command source={} amount={} currency={}",
                TraceContextHolder.currentTraceId(), command.sourceSystem(), command.amount(), command.currency());

        PythonWorkerClient.ReceiptData receiptData = null;
        if (command.imagePayload() != null) {
            receiptData = pythonWorkerClient.scanReceipt(command.imagePayload());
        }

        BigDecimal amount = resolveAmount(command, receiptData);
        validateBudget(amount);

        FireflyExpenseCommand expenseCommand = FireflyExpenseCommand.builder()
                .amount(amount)
                .currency(resolveCurrency(command, receiptData))
                .description(command.description())
                .category(command.category())
                .sourceAccount(command.sourceAccount() == null ? fireflyProperties.getDefaultSourceAccount() : command.sourceAccount())
                .destinationAccount(fireflyProperties.getDefaultDestinationAccount())
                .occurredAt(command.occurredAt())
                .build();
        FireflyClient.FireflyTransactionResult result = fireflyClient.createExpense(expenseCommand);

        WealthEntry entry = WealthEntry.builder()
                .entryType(WealthEntry.EntryType.EXPENSE)
                .sourceSystem(command.sourceSystem())
                .rawContent(command.description())
                .structuredData(Map.of(
                        "firefly_journal_ids", result.journalIds(),
                        "receipt", receiptData,
                        "category", command.category()))
                .amount(amount)
                .currency(expenseCommand.currency())
                .occurredOn(command.occurredAt().toLocalDate())
                .embedding(toList(embeddingModel.embed(command.description())))
                .tags(command.tags())
                .build();
        return wealthRepository.save(entry);
    }

    private List<Double> toList(float[] embedding) {
        if (embedding == null) return List.of();
        List<Double> list = new ArrayList<>(embedding.length);
        for (float f : embedding) {
            list.add((double) f);
        }
        return list;
    }

    private void validateBudget(BigDecimal amount) {
        FireflyClient.BalanceSnapshot snapshot = fireflyClient.fetchBalances();
        BigDecimal total = snapshot.entries().stream()
                .map(FireflyClient.BalanceEntry::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(amount) < 0) {
            throw new ValidationException("Budget insufficient for expense");
        }
    }

    private BigDecimal resolveAmount(ExpenseCommand command, PythonWorkerClient.ReceiptData receiptData) {
        if (command.amount() != null) {
            return command.amount();
        }
        if (receiptData != null) {
            return BigDecimal.valueOf(receiptData.amount());
        }
        throw new ValidationException("Amount not provided and cannot be inferred");
    }

    private String resolveCurrency(ExpenseCommand command, PythonWorkerClient.ReceiptData receiptData) {
        if (command.currency() != null) {
            return command.currency();
        }
        if (receiptData != null && receiptData.currency() != null) {
            return receiptData.currency();
        }
        return fireflyProperties.getCurrency();
    }

    public record ExpenseCommand(String description,
                                 String category,
                                 BigDecimal amount,
                                 String currency,
                                 String sourceAccount,
                                 OffsetDateTime occurredAt,
                                 String sourceSystem,
                                 List<String> tags,
                                 PythonWorkerClient.ImagePayload imagePayload) {
    }
}
