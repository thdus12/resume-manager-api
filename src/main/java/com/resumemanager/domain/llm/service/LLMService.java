package com.resumemanager.domain.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class LLMService {

    @Value("${claude.api.key}")
    private String claudeApiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    /**
     * Claude API 호출
     */
    public String query(String systemPrompt, String userPrompt) {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(Duration.ofSeconds(60));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader("x-api-key", claudeApiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "max_tokens", 4096,
                "system", systemPrompt,
                "messages", new Object[]{
                        Map.of("role", "user", "content", userPrompt)
                }
        );

        log.info("Claude API 호출 시작 - 모델: {}", MODEL);
        long startTime = System.currentTimeMillis();

        String responseBody = client.post()
                .uri(CLAUDE_API_URL)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Claude API 호출 완료 - 소요시간: {}ms", elapsed);

        JSONObject resp = new JSONObject(responseBody);
        JSONArray content = resp.getJSONArray("content");
        return content.getJSONObject(0).getString("text");
    }
}
