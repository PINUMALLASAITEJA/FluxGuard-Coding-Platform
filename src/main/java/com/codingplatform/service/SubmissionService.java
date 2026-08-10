package com.codingplatform.service;

import java.util.List;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;

public interface SubmissionService {

    Submission submit(Submission submission);

    List<Submission> getByUser(UserAccount user);
}
