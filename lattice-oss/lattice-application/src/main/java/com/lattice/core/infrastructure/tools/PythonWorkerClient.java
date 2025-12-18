package com.lattice.core.infrastructure.tools;

import com.lattice.core.infrastructure.exception.ExternalServiceException;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Thin REST facade over the Python worker cluster.
 */
@Component
public class PythonWorkerClient {

    private static final Logger log = LoggerFactory.getLogger(PythonWorkerClient.class);

    private final RestClient restClient;

    public PythonWorkerClient(RestClient.Builder builder, PythonWorkerProperties properties) {
        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ReceiptData scanReceipt(ImagePayload payload) {
        try {
            log.info("[traceId={}] Invoking Python receipt OCR", TraceContextHolder.currentTraceId());
            return restClient.post()
                    .uri("/api/ocr/receipt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(ReceiptData.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Receipt OCR failed", ex);
        }
    }

    public ResumeInsights parseResume(ResumeDocument document) {
        try {
            log.info("[traceId={}] Invoking Python resume parser", TraceContextHolder.currentTraceId());
            return restClient.post()
                    .uri("/api/parse-resume")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(document)
                    .retrieve()
                    .body(ResumeInsights.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Resume parsing failed", ex);
        }
    }

    public record ImagePayload(byte[] content, String filename, String contentType) {
    }

    public record ReceiptData(String merchant,
                              double amount,
                              String currency,
                              Map<String, Object> metadata) {
    }

    public record ResumeDocument(String text, String format, Map<String, Object> metadata) {
    }

    public record ResumeInsights(Map<String, Object> star, Map<String, Object> skills) {
    }
}
