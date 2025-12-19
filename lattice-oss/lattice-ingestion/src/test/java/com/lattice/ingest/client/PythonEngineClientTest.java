package com.lattice.ingest.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({PythonEngineClient.class, PythonEngineProperties.class})
class PythonEngineClientTest {

    @Autowired
    private PythonEngineClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void testAnalyze() {
        // 准备 Mock 响应
        String jsonResponse = """
                {
                  "content": "This is a test content from Python",
                  "metadata": {"author": "Lattice"},
                  "status": "success"
                }
                """;

        this.server.expect(requestTo("http://python-worker:8000/engine/analyze"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // 执行调用
        ByteArrayResource resource = new ByteArrayResource("dummy content".getBytes()) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };
        AnalysisResult result = client.analyze(resource);

        // 验证结果
        assertThat(result.content()).isEqualTo("This is a test content from Python");
        assertThat(result.metadata()).containsEntry("author", "Lattice");
        assertThat(result.status()).isEqualTo("success");
    }
}
