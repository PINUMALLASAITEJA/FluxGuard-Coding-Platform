package com.codingplatform.service.impl;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.codingplatform.service.CodeExecutionService;

@Service
public class Judge0CodeExecutionService implements CodeExecutionService {

    private final RestClient restClient;
    private final boolean enabled;
    private final String apiKey;

    public Judge0CodeExecutionService(@Value("${judge0.url:https://ce.judge0.com}") String url,
                                      @Value("${judge0.enabled:false}") boolean enabled,
                                      @Value("${judge0.api-key:}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(url).build();
        this.enabled = enabled;
        this.apiKey = apiKey;
    }

    @Override
    public ExecutionResult execute(String sourceCode, String language, String stdin, String expectedOutput) {
        if (!enabled) {
            return new ExecutionResult(null, "PENDING", null, null, null, null, null);
        }

        Integer languageId = languageId(language);
        if (languageId == null) {
            return new ExecutionResult(null, "SYSTEM_ERROR", null, "Unsupported language: " + language,
                    null, null, null);
        }

        Map<String, Object> request = Map.of(
                "language_id", languageId,
                "source_code", sourceCode,
                "stdin", stdin == null ? "" : stdin,
                "expected_output", expectedOutput == null ? "" : expectedOutput,
                "cpu_time_limit", 2,
                "wall_time_limit", 5,
                "memory_limit", 128000,
                "enable_network", false
        );

        try {
            Judge0Response response = restClient.post().uri("/submissions?wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (!apiKey.isBlank()) {
                            headers.set("X-Auth-Token", apiKey);
                        }
                    })
                    .body(request)
                    .retrieve()
                    .body(Judge0Response.class);
            if (response == null) {
                return new ExecutionResult(null, "SYSTEM_ERROR", null, "Empty Judge0 response.", null, null, null);
            }
            String verdict = response.status() == null ? "SYSTEM_ERROR" : verdict(response.status().id());
            return new ExecutionResult(response.token(), verdict, response.stdout(), response.stderr(),
                    response.compileOutput(), toMillis(response.time()), response.memory());
        } catch (RuntimeException exception) {
            return new ExecutionResult(null, "SYSTEM_ERROR", null, exception.getMessage(), null, null, null);
        }
    }

    private Integer languageId(String language) {
        return switch (language.trim().toLowerCase()) {
            case "python", "python3" -> 71;
            case "java" -> 62;
            case "c" -> 50;
            case "c++", "cpp" -> 54;
            default -> null;
        };
    }

    private String verdict(Integer statusId) {
        return switch (statusId) {
            case 3 -> "ACCEPTED";
            case 4 -> "WRONG_ANSWER";
            case 5 -> "TIME_LIMIT_EXCEEDED";
            case 6 -> "COMPILATION_ERROR";
            case 7, 8, 9, 10, 11, 12 -> "RUNTIME_ERROR";
            default -> "SYSTEM_ERROR";
        };
    }

    private Long toMillis(String seconds) {
        if (seconds == null) {
            return null;
        }
        return Math.round(Double.parseDouble(seconds) * 1000);
    }

    public record Judge0Response(String token, String stdout, String stderr,
                                 @JsonProperty("compile_output") String compileOutput,
                                 String time, Long memory, Judge0Status status) {
    }

    public record Judge0Status(Integer id, String description) {
    }
}