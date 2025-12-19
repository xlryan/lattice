package com.lattice.ingest.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        log.info("Sending file to Python Engine for analysis: {}", file.getFilename());
        
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file);

        return restClient.post()
                .uri("/engine/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .body(AnalysisResult.class);
    }
}
