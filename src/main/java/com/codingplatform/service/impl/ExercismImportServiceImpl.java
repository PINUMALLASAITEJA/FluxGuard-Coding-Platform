package com.codingplatform.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.model.Problem;
import com.codingplatform.repository.ProblemSourceRepository;
import com.codingplatform.service.ExercismImportService;

@Service
public class ExercismImportServiceImpl implements ExercismImportService {

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
        int imported = 0;
        for (String slug : EXERCISES) {
            try {
                String description = download(slug);
                Problem problem = problemRepository.findBySourceAndSourceId(SOURCE, slug).orElseGet(Problem::new);
                problem.setSource(SOURCE);
                problem.setSourceId(slug);
                problem.setSourceUrl("https://github.com/exercism/problem-specifications/tree/" + REVISION
                        + "/exercises/" + slug);
                problem.setLicense("MIT");
                problem.setTitle(titleFrom(slug, description));
                problem.setDifficulty("Practice");
                problem.setDescription(description);
                problemRepository.save(problem);
                imported++;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Exercism import interrupted.", exception);
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to import Exercism exercise: " + slug, exception);
            }
        }
        return imported;
    }

    private String download(String slug) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(RAW_BASE + slug + "/description.md")).GET().build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("GitHub returned HTTP " + response.statusCode());
        }
        return response.body().trim();
    }

    private String titleFrom(String slug, String description) {
        return description.lines().map(String::trim).filter(line -> line.startsWith("# ")).findFirst()
                .map(line -> line.substring(2).trim()).orElseGet(() -> slug.replace('-', ' '));
    }
}