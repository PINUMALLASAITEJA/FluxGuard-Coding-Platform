package com.codingplatform.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.repository.UserAccountRepository;
import com.codingplatform.service.SubmissionService;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserAccountRepository userAccountRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, ProblemRepository problemRepository,
                                 UserAccountRepository userAccountRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public Submission submitSolution(UserAccount user, Long problemId, String code, String language) {
        if (user == null || user.getId() == null || !userAccountRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("You must be logged in to submit a solution.");
        }
        if (!problemRepository.existsById(problemId)) {
            throw new IllegalArgumentException("The selected problem does not exist.");
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Please select a programming language.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Please enter your solution code.");
        }

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problemRepository.findById(problemId).orElseThrow());
        submission.setCode(code.trim());
        submission.setLanguage(language.trim());
        submission.setStatus("PENDING");
        submission.setSubmittedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getUserSubmissions(UserAccount user) {
        return submissionRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getSubmissionById(Long id, UserAccount user) {
        return submissionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getRecentSubmissions(UserAccount user) {
        return submissionRepository.findTop5ByUserOrderBySubmittedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUserSubmissions(UserAccount user) {
        return submissionRepository.countByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int countSolvedProblems(UserAccount user) {
        return submissionRepository.countDistinctProblemsByUserAndStatus(user, "ACCEPTED");
    }
}
