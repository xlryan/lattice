package com.lattice.core.interfaces.dashboard;

import com.lattice.core.interfaces.shared.ApiResponse;
import com.lattice.core.repository.career.CareerRepository;
import com.lattice.core.repository.wealth.WealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CareerRepository careerRepository;
    private final WealthRepository wealthRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        long nodeCount = careerRepository.count();
        long wealthCount = wealthRepository.count();

        DashboardStatsResponse stats = new DashboardStatsResponse(
                "¥824,592", // Mocked net worth for now
                (int) nodeCount,
                8, // Mocked active projects
                92, // Mocked health score
                List.of(
                        new MonthlyFlow("周一", 4000, 2400),
                        new MonthlyFlow("周二", 3000, 1398),
                        new MonthlyFlow("周三", 2000, 9800),
                        new MonthlyFlow("周四", 2780, 3908),
                        new MonthlyFlow("周五", 1890, 4800),
                        new MonthlyFlow("周六", 2390, 3800),
                        new MonthlyFlow("周日", 3490, 4300)
                ),
                List.of(
                        new RecentUpdate("1", "更新了职业生涯节点 \"高级工程师\"", "2 小时前"),
                        new RecentUpdate("2", "记了一笔 \"咖啡\" 支出 ¥18.5", "5 小时前")
                )
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
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
