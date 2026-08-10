package com.codingplatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    int countByUserAndStatus(UserAccount user, String status);

    int countByUser(UserAccount user);

    List<Submission> findTop5ByUserOrderBySubmittedAtDesc(UserAccount user);

    List<Submission> findByUser(UserAccount user);
}
