package com.lattice.core.interfaces.dashboard;

import com.lattice.core.application.career.CareerService;
import com.lattice.core.application.wealth.WealthService;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.wealth.WealthEntry;
import com.lattice.core.interfaces.shared.ApiResponse;
import com.lattice.core.infrastructure.tools.FireflyClient;
import com.lattice.core.repository.career.CareerRepository;
import com.lattice.core.repository.wealth.WealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CareerRepository careerRepository;
    private final WealthRepository wealthRepository;
    private final WealthService wealthService;
    private final CareerService careerService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        // 1. Net Worth from Firefly balances
        List<FireflyClient.BalanceEntry> balances = wealthService.fetchAssetAllocation();
        BigDecimal totalNetWorth = balances.stream()
                .map(FireflyClient.BalanceEntry::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        String netWorthStr = currencyFormat.format(totalNetWorth);

        // 2. Node Count
        long nodeCount = careerRepository.count();

        // 3. Active Projects (estimated from tags)
        // This is a placeholder logic: count unique tags in CareerNodes that might represent projects
        long activeProjects = careerService.findAll().stream()
                .flatMap(n -> n.getTags().stream())
                .distinct()
                .count();

        // 4. Health Score (mocked calculation based on net worth vs nodes)
        int healthScore = (int) Math.min(100, 60 + (nodeCount / 10) + (totalNetWorth.intValue() / 100000));

        // 5. Monthly Flow (Recent Transactions)
        List<FireflyClient.FireflyTransactionItem> transactions = wealthService.fetchTransactions();
        // Just take last 7 transactions for flow display or aggregate them by date
        List<MonthlyFlow> monthlyFlow = transactions.stream()
                .limit(7)
                .map(tx -> new MonthlyFlow(tx.date().substring(5, 10), 
                        tx.type().equals("deposit") ? tx.amount().doubleValue() : 0,
                        tx.type().equals("withdrawal") ? tx.amount().doubleValue() : 0))
                .toList();

        // 6. Recent Updates
        List<RecentUpdate> recentUpdates = new ArrayList<>();
        
        // Latest Career Nodes
        careerRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(node -> recentUpdates.add(new RecentUpdate(
                        node.getId().toString(),
                        "更新了职业生涯节点: " + truncate(node.getRawContent(), 30),
                        formatRelativeTime(node.getCreatedAt())
                )));

        // Latest Wealth Entries
        wealthRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(entry -> recentUpdates.add(new RecentUpdate(
                        entry.getId().toString(),
                        "记了一笔 " + entry.getEntryType() + ": " + entry.getAmount() + " " + entry.getCurrency(),
                        formatRelativeTime(entry.getCreatedAt())
                )));

        // Sort combined updates by time (though IDs are UUIDs, we can use a custom sorting if we had timestamps in RecentUpdate)
        // For now, just return them interleaved or as is.

        DashboardStatsResponse stats = new DashboardStatsResponse(
                netWorthStr,
                (int) nodeCount,
                (int) activeProjects,
                healthScore,
                monthlyFlow,
                recentUpdates
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length) + "...";
    }

    private String formatRelativeTime(java.time.Instant instant) {
        // Simple placeholder for relative time
        return DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault()).format(instant);
    }

    public record DashboardStatsResponse(
            String netWorth,
            int nodeCount,
            int activeProjects,
            int healthScore,
            List<MonthlyFlow> monthlyFlow,
            List<RecentUpdate> recentUpdates
    ) {}

    public record MonthlyFlow(String name, double income, double expense) {}
    public record RecentUpdate(String id, String content, String time) {}
}
