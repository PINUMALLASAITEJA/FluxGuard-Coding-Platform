package com.codingplatform.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.codingplatform.model.Problem;
import com.codingplatform.repository.ProblemSourceRepository;
import com.codingplatform.service.ExercismImportService;

@Service
public class ExercismImportServiceImpl implements ExercismImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExercismImportServiceImpl.class);
    private static final String SOURCE = "EXERCISM";
    private static final String REVISION = "03f83310ed0547b902d211a05d23b3d74661df02";
    private static final String RAW_BASE = "https://raw.githubusercontent.com/exercism/problem-specifications/"
            + REVISION + "/exercises/";
    private static final List<String> EXERCISES = List.of(
            "hello-world", "difference-of-squares", "sum-of-multiples", "leap", "raindrops",
            "space-age", "grains", "hamming", "rna-transcription", "isogram", "pangram",
            "acronym", "reverse-string", "scrabble-score", "series", "luhn", "etl",
            "phone-number", "word-count", "all-your-base", "anagram", "atbash-cipher",
            "binary-search", "bracket-push", "collatz-conjecture", "crypto-square", "diamond",
            "gigasecond", "kindergarten-garden", "matching-brackets"
    );

    private final ProblemSourceRepository problemRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ExercismImportServiceImpl(ProblemSourceRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    @Transactional
    public int importProblems() {
        logger.info("Exercism import service started transactionActive={}",
                TransactionSynchronizationManager.isActualTransactionActive());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                logger.info("Exercism import transaction committed");
            }

            @Override
            public void afterCompletion(int status) {
                logger.info("Exercism import transaction completed status={}", status);
            }
        });
        int imported = 0;
        for (String slug : EXERCISES) {
            try {
                String description = downloadDescription(slug);
                logger.info("Exercism problem parsed sourceId={} descriptionLength={}", slug, description.length());
                Problem problem = problemRepository.findBySourceAndSourceId(SOURCE, slug).orElseGet(Problem::new);
                problem.setSource(SOURCE);
                problem.setSourceId(slug);
                problem.setSourceUrl("https://github.com/exercism/problem-specifications/tree/" + REVISION
                        + "/exercises/" + slug);
                problem.setLicense("MIT");
                problem.setTitle(titleFrom(slug, description));
                problem.setDifficulty("Practice");
                problem.setDescription(description);
                logger.info("Saving Exercism problem sourceId={} existingId={}", slug, problem.getId());
                Problem savedProblem = problemRepository.save(problem);
                logger.info("Saved Exercism problem sourceId={} id={}", slug, savedProblem.getId());
                imported++;
                logger.info("Imported Exercism problem source={} sourceId={} title={}", SOURCE, slug,
                        problem.getTitle());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                logger.error("Exercism import interrupted sourceId={}", slug, exception);
                throw new IllegalStateException("Exercism import interrupted.", exception);
            } catch (Exception exception) {
                logger.error("Unable to import Exercism exercise sourceId={} causeType={} causeMessage={}", slug,
                        exception.getClass().getName(), exception.getMessage(), exception);
            }
        }
        if (imported == 0) {
            throw new IllegalStateException("No Exercism problems were imported. Check the logs for the first failure.");
        }
        return imported;
    }

    private String downloadDescription(String slug) throws Exception {
        for (String filename : List.of("description.md", "instructions.md", "introduction.md")) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(RAW_BASE + slug + "/" + filename)).GET().build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            logger.info("Exercism source request sourceId={} file={} status={}", slug, filename, response.statusCode());
            if (response.statusCode() == 200 && !response.body().isBlank()) {
                return response.body().trim();
            }
        }
        throw new IllegalStateException("No supported markdown file found for Exercism exercise: " + slug);
    }

    private String titleFrom(String slug, String description) {
        String title = description.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .filter(this::isMeaningfulTitle)
                .findFirst()
                .orElse(null);

        if (title != null) {
            return title;
        }

        return toDisplayTitle(slug);
    }

    private boolean isMeaningfulTitle(String value) {
        String normalized = value.trim();
        return !normalized.equalsIgnoreCase("Description")
                && !normalized.equalsIgnoreCase("Instructions")
                && !normalized.equalsIgnoreCase("Introduction")
                && !normalized.isBlank();
    }

    private String toDisplayTitle(String slug) {
        return Arrays.stream(slug.split("-"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
}