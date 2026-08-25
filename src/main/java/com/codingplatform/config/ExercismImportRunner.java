package com.codingplatform.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codingplatform.service.ExercismImportService;

@Configuration
public class ExercismImportRunner {

    @Bean
    @ConditionalOnProperty(name = "exercism.import.enabled", havingValue = "true")
    CommandLineRunner importExercismProblems(ExercismImportService importService) {
        return args -> System.out.println("Imported " + importService.importProblems() + " Exercism problems.");
    }
}