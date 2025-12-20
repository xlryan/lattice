package com.lattice.core.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class PythonEngineClient {

    private final RestClient restClient;

    public PythonEngineClient(PythonEngineProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    /**
     * 调用 Python 引擎分析文档
     * @param file 资源文件
     * @return 分析结果
     */
    public AnalysisResult analyze(Resource file) {
        log.info("Requesting Python Engine analysis for file: {}", file.getFilename());
        long start = System.currentTimeMillis();
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file);

        try {
            AnalysisResult result = restClient.post()
                    .uri("/engine/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .retrieve()
                    .body(AnalysisResult.class);
            
            long duration = System.currentTimeMillis() - start;
            log.info("Python Engine analysis completed in {}ms. Status: {}", duration, (result != null ? result.status() : "null"));
            return result;
        } catch (Exception e) {
            log.error("Failed to call Python Engine /engine/analyze", e);
            throw e;
        }
    }

    /**
     * 调用 Python 引擎进行文本向量化
     * @param text 文本
     * @return 向量
     */
    public List<Double> embed(String text) {
        log.debug("Requesting Python Engine embedding for text (length={})", text.length());
        long start = System.currentTimeMillis();
        
        try {
            EmbedResponse response = restClient.post()
                    .uri("/engine/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EmbedRequest(text))
                    .retrieve()
                    .body(EmbedResponse.class);
            
            long duration = System.currentTimeMillis() - start;
            int dim = (response != null && response.vector() != null) ? response.vector().size() : 0;
            log.info("Python Engine embedding completed in {}ms. Dimension: {}", duration, dim);
            return response != null ? response.vector() : List.of();
        } catch (Exception e) {
            log.error("Failed to call Python Engine /engine/embed", e);
            throw e;
        }
    }
}
