package com.lattice.core.interfaces.ingestion;

import com.lattice.core.application.career.CareerService;
import com.lattice.core.application.wealth.WealthService;
import com.lattice.core.application.wealth.WealthService.ExpenseCommand;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.infrastructure.tools.PythonWorkerClient;
import com.lattice.core.interfaces.shared.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Tag(name = "Ingestion")
@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final PythonWorkerClient pythonWorkerClient;
    private final CareerService careerService;
    private final WealthService wealthService;

    public IngestionController(PythonWorkerClient pythonWorkerClient,
                               CareerService careerService,
                               WealthService wealthService) {
        this.pythonWorkerClient = pythonWorkerClient;
        this.careerService = careerService;
        this.wealthService = wealthService;
    }

    @Operation(summary = "Upload resume PDF and extract STAR entries")
    @PostMapping(value = "/career/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> ingestResume(@RequestPart("file") MultipartFile file) throws IOException {
        String encoded = Base64.getEncoder().encodeToString(file.getBytes());
        PythonWorkerClient.ResumeDocument document = new PythonWorkerClient.ResumeDocument(
                encoded,
                file.getContentType(),
                Map.of("filename", file.getOriginalFilename())
        );
        var insights = pythonWorkerClient.parseResume(document);
        careerService.createLog(insights.star().toString(), CareerType.RESUME_ITEM, List.of("resume"));
        return ApiResponse.success("Resume ingested");
    }

    @Operation(summary = "Upload receipt image for wealth entry",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ReceiptIngestRequest.class))))
    @PostMapping(value = "/wealth/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> ingestReceipt(@Valid @RequestPart("request") ReceiptIngestRequest request,
                                             @RequestPart("file") MultipartFile file) throws IOException {
        PythonWorkerClient.ImagePayload payload = new PythonWorkerClient.ImagePayload(
                file.getBytes(), file.getOriginalFilename(), file.getContentType());
        ExpenseCommand command = new ExpenseCommand(
                request.description(),
                request.category(),
                null,
                null,
                null,
                OffsetDateTime.now(),
                "ingestion",
                List.of("receipt"),
                payload
        );
        wealthService.ingestExpense(command);
        return ApiResponse.success("Receipt ingested");
    }

    @Schema(name = "ReceiptIngestRequest")
    public record ReceiptIngestRequest(@NotBlank String description, String category) {
    }
}
