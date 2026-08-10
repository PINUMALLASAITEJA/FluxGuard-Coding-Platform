package com.codingplatform.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;
import com.codingplatform.repository.SubmissionRepository;
import com.codingplatform.service.SubmissionService;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public Submission submit(Submission submission) {
        if (submission.getStatus() == null || submission.getStatus().isBlank()) {
            submission.setStatus("PENDING");
        }
        if (submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(LocalDateTime.now());
        }
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getByUser(UserAccount user) {
        return submissionRepository.findByUser(user);
    }
}
