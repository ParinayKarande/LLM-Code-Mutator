import Interfaces.LLMApiService;
import Interfaces.LoggerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenAIServiceTest {
    LLMApiService openAIService;
    LoggerService mockLoggerService;
    List<String> logs;

    @BeforeEach
    void setUp() {
        mockLoggerService = new LoggerService() {
            @Override
            public void log(String message) {
                logs.add(message);
            }

            @Override
            public void error(String message) {
                logs.add(message);
            }

            @Override
            public void debug(String message) {
                logs.add(message);
            }
        };
        logs = new LinkedList<>();

        openAIService = new OpenAIService("mockModelName"
                , "MockApiKey"
                , "https://mock/URL/"
                , HttpClients.createDefault()
                , mockLoggerService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void askOpenAI() throws IOException {
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);

        String mockJsonString = new JSONObject()
                .put("choices", new JSONArray().put(new JSONObject()
                        .put("message", new JSONObject().put("content", "Mocked mutated code"))))
                .put("usage", new JSONObject()
                        .put("prompt_tokens", 10)
                        .put("completion_tokens", 5)
                        .put("total_tokens", 15))
                .toString();

        InputStream mockStream = new ByteArrayInputStream(mockJsonString.getBytes());
        when(mockEntity.getContent()).thenReturn(mockStream);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        openAIService = new OpenAIService("mockModelName"
                , "MockApiKey"
                , "https://mock/URL/"
                , mockHttpClient
                , mockLoggerService);
        String openAIResponse = openAIService.askOpenAI("");



        assertNotNull(openAIResponse);
        assertTrue(logs.stream().anyMatch(log -> log.contains("API response received")));
    }
}