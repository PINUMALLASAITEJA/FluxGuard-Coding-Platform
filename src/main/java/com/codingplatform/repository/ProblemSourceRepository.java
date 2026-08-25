package com.codingplatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codingplatform.model.Problem;

public interface ProblemSourceRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findBySourceAndSourceId(String source, String sourceId);
}
