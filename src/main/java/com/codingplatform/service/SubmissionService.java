package com.codingplatform.service;

import java.util.List;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;

public interface SubmissionService {

    Submission submitSolution(UserAccount user, Long problemId, String code, String language);

    Submission submitSolution(UserAccount user, Long problemId, String code, String language, String stdin);

    List<Submission> getUserSubmissions(UserAccount user);

    Submission getSubmissionById(Long id, UserAccount user);

    List<Submission> getRecentSubmissions(UserAccount user);

    int countUserSubmissions(UserAccount user);

    int countAcceptedSubmissions(UserAccount user);

    int countSolvedProblems(UserAccount user);
}
