package com.codingplatform.service;

public interface CodeExecutionService {
    ExecutionResult execute(String sourceCode, String language, String stdin, String expectedOutput);

    record ExecutionResult(String token, String status, String stdout, String stderr, String compileOutput,
                           Long executionTimeMs, Long memoryKb) {
    }
}