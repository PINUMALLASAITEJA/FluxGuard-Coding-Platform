package com.codingplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codingplatform.model.Problem;
import com.codingplatform.model.ProblemTestCase;

public interface ProblemTestCaseRepository extends JpaRepository<ProblemTestCase, Long> {
    List<ProblemTestCase> findByProblemAndPublicCaseTrueOrderByOrderIndex(Problem problem);
}