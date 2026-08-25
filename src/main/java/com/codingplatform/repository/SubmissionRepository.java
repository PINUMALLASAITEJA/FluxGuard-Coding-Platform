package com.codingplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codingplatform.model.Submission;
import com.codingplatform.model.UserAccount;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    int countByUserAndStatus(UserAccount user, String status);

    int countByUser(UserAccount user);

    @org.springframework.data.jpa.repository.Query("select count(distinct s.problem.id) from Submission s where s.user = :user and s.status = :status")
    int countDistinctProblemsByUserAndStatus(UserAccount user, String status);

    List<Submission> findTop5ByUserOrderBySubmittedAtDesc(UserAccount user);

    List<Submission> findByUser(UserAccount user);

    Optional<Submission> findByIdAndUser(Long id, UserAccount user);
}
