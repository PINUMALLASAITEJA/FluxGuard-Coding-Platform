package com.codingplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codingplatform.repository.ProblemSourceRepository;
import com.codingplatform.service.ExercismImportService;

@SpringBootTest
class CodingPlatformApplicationTests {

	@Autowired
	private ExercismImportService exercismImportService;

	@Autowired
	private ProblemSourceRepository problemRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void importsAtLeastOneProblemFromExercism() {
		int imported = exercismImportService.importProblems();

		assertTrue(imported > 0);
		assertTrue(problemRepository.findBySourceAndSourceId("EXERCISM", "hello-world").isPresent());
	}

}
