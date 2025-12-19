package com.lattice.core.interfaces.wealth;

import com.lattice.core.application.wealth.WealthService;
import com.lattice.core.infrastructure.tools.FireflyClient;
import com.lattice.core.interfaces.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wealth")
@RequiredArgsConstructor
public class WealthController {

    private final WealthService wealthService;

    @GetMapping("/assets")
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getAssets() {
        List<FireflyClient.BalanceEntry> entries = wealthService.fetchAssetAllocation();
        List<AssetResponse> response = entries.stream()
                .map(e -> new AssetResponse(e.accountName(), e.balance().doubleValue()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expenses/monthly")
    public ResponseEntity<ApiResponse<List<WealthService.MonthlyExpense>>> getMonthlyExpenses() {
        return ResponseEntity.ok(ApiResponse.success(wealthService.fetchMonthlyExpenses()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<FireflyClient.FireflyTransactionItem>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.success(wealthService.fetchTransactions()));
    }

    public record AssetResponse(String label, double value) {}
}
