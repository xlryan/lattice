package com.lattice.core.application.wealth;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.agent.finance.model.FireflyExpenseCommand;
import com.lattice.core.domain.wealth.WealthEntry;
import com.lattice.core.infrastructure.client.PythonEngineClient;
import com.lattice.core.infrastructure.exception.ValidationException;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.tools.FireflyClient;
import com.lattice.core.infrastructure.tools.PythonWorkerClient;
import com.lattice.core.repository.wealth.WealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private final PythonEngineClient pythonEngineClient;

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
                .embedding(pythonEngineClient.embed(command.description()))
                .tags(command.tags())
                .build();
        return wealthRepository.save(entry);
    }

    public List<FireflyClient.BalanceEntry> fetchAssetAllocation() {
        return fireflyClient.fetchBalances().entries();
    }

    public List<MonthlyExpense> fetchMonthlyExpenses() {
        // Mocking monthly expenses for now as Firefly might need complex queries
        return List.of(
                new MonthlyExpense("2025-07", new BigDecimal("1200.00")),
                new MonthlyExpense("2025-08", new BigDecimal("1500.00")),
                new MonthlyExpense("2025-09", new BigDecimal("1100.00")),
                new MonthlyExpense("2025-10", new BigDecimal("2300.00")),
                new MonthlyExpense("2025-11", new BigDecimal("1800.00")),
                new MonthlyExpense("2025-12", new BigDecimal("2100.00"))
        );
    }

    public List<FireflyClient.FireflyTransactionItem> fetchTransactions() {
        return fireflyClient.fetchTransactions();
    }

    public record MonthlyExpense(String month, BigDecimal amount) {}

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
