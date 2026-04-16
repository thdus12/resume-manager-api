package com.resumemanager.domain.llm.service;

import io.netty.resolver.DefaultAddressResolverGroup;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LLMService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";

    /**
     * OpenAI Chat Completions API 호출
     */
    public String query(String systemPrompt, String userPrompt) {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(Duration.ofSeconds(60));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        log.info("OpenAI API 호출 시작 - 모델: {}", MODEL);
        long startTime = System.currentTimeMillis();

        String responseBody = client.post()
                .uri(OPENAI_API_URL)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("OpenAI API 호출 완료 - 소요시간: {}ms", elapsed);

        JSONObject resp = new JSONObject(responseBody);
        JSONArray choices = resp.getJSONArray("choices");
        String content = choices.getJSONObject(0).getJSONObject("message").getString("content");

        if (resp.has("usage")) {
            JSONObject usage = resp.getJSONObject("usage");
            log.info("토큰 사용량 - prompt: {}, completion: {}, total: {}",
                    usage.optInt("prompt_tokens", 0),
                    usage.optInt("completion_tokens", 0),
                    usage.optInt("total_tokens", 0));
        }

        return content;
    }
}
