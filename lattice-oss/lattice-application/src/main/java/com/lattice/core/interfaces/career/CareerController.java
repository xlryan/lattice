jixupackage com.lattice.core.interfaces.career;

import com.lattice.core.application.career.CareerService;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.interfaces.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/career")
public class CareerController {

    private final CareerService careerService;

    public CareerController(CareerService careerService) {
        this.careerService = careerService;
    }

    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<CareerNodeResponse>> createLog(@Valid @RequestBody CreateCareerLogRequest request) {
        CareerNode node = careerService.createLog(request.rawText(), request.type(), request.tags());
        return ResponseEntity.ok(ApiResponse.success(toResponse(node)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CareerNodeResponse>>> search(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(value = "limit", defaultValue = "5") @Min(1) int limit) {
        List<CareerNode> nodes = careerService.semanticSearch(query, limit);
        return ResponseEntity.ok(ApiResponse.success(nodes.stream().map(this::toResponse).toList()));
    }

    private CareerNodeResponse toResponse(CareerNode node) {
        return new CareerNodeResponse(
                node.getId(),
                node.getType(),
                node.getRawContent(),
                node.getStructuredData(),
                node.getCreatedAt(),
                node.getTags()
        );
    }

    public record CreateCareerLogRequest(
            @NotBlank String rawText,
            @NotNull CareerType type,
            List<String> tags
    ) {
    }

    public record CareerNodeResponse(
            UUID id,
            CareerType type,
            String rawContent,
            Map<String, Object> structuredData,
            Instant createdAt,
            List<String> tags
    ) {
    }
}
