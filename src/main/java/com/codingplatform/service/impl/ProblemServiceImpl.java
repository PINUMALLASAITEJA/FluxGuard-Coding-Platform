package com.codingplatform.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.model.Problem;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.service.ProblemService;

@Service
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemServiceImpl(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Problem getProblemById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found."));
    }

    @Override
    @Transactional
    public Problem saveProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    @Override
    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new IllegalArgumentException("Problem not found.");
        }
        problemRepository.deleteById(id);
    }
}
