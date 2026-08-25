package com.codingplatform.service;

import java.util.List;

import com.codingplatform.model.Problem;

public interface ProblemService {

    List<Problem> getAllProblems();

    Problem getProblemById(Long id);

    Problem saveProblem(Problem problem);

    void deleteProblem(Long id);
}
