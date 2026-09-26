package com.codingplatform.fluxguard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codingplatform.fluxguard.model.FluxGuardRequestLog;

@Repository
public interface FluxGuardRequestLogRepository extends JpaRepository<FluxGuardRequestLog, Long> {

    List<FluxGuardRequestLog> findByUserIdOrderByTimestampDesc(Long userId);

        long countByTimestampGreaterThanEqualAndTimestampLessThan(LocalDateTime from, LocalDateTime to);

        long countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusBetween(
            LocalDateTime from, LocalDateTime to, int minStatus, int maxStatus);

        long countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqual(
            LocalDateTime from, LocalDateTime to, int status);

        List<FluxGuardRequestLog> findTop100ByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqualOrderByTimestampDesc(
            LocalDateTime from, LocalDateTime to, int status);

        @Query("select count(distinct log.sessionId) from FluxGuardRequestLog log "
            + "where log.timestamp >= :since and log.sessionId is not null and log.sessionId <> 'none'")
        long countActiveSessionsSince(@Param("since") LocalDateTime since);

        @Query("select log.instanceId as instanceId, count(distinct log.sessionId) as activeUsers "
            + "from FluxGuardRequestLog log where log.timestamp >= :since "
            + "and log.sessionId is not null and log.sessionId <> 'none' and log.instanceId is not null "
            + "group by log.instanceId order by log.instanceId")
        List<InstanceUserCount> findInstanceUserCountsSince(@Param("since") LocalDateTime since);

        List<FluxGuardRequestLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

        interface InstanceUserCount {
        String getInstanceId();
        long getActiveUsers();
    }
}
