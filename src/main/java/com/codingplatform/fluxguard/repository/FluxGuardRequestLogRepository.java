package com.codingplatform.fluxguard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codingplatform.fluxguard.model.FluxGuardRequestLog;

@Repository
public interface FluxGuardRequestLogRepository extends JpaRepository<FluxGuardRequestLog, Long> {

    List<FluxGuardRequestLog> findTop20ByUserIdOrderByTimestampDesc(Long userId);

    List<FluxGuardRequestLog> findTop20ByOrderByTimestampDesc();

    List<FluxGuardRequestLog> findByUserIdOrderByTimestampDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndResponseStatusBetween(Long userId, int minStatus, int maxStatus);

    default long countByUserIdAndTimestampAfter(Long userId, LocalDateTime since) {
        return findByUserIdOrderByTimestampDesc(userId).stream()
                .filter(log -> log.getTimestamp() != null && !log.getTimestamp().isBefore(since))
                .count();
    }
}
