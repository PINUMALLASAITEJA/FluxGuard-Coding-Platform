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

    @Query("select count(log) from FluxGuardRequestLog log "
        + "where log.timestamp >= :from and log.timestamp < :to "
        + "and log.endpoint not like '/favicon.ico' "
        + "and log.endpoint not like '/css/%' "
        + "and log.endpoint not like '/js/%' "
        + "and log.endpoint not like '/images/%' "
        + "and log.endpoint not like '/webjars/%'")
    long countRelevantRequestsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(log) from FluxGuardRequestLog log "
        + "where log.timestamp >= :from and log.timestamp < :to "
        + "and log.responseStatus between :min and :max "
        + "and log.endpoint not like '/favicon.ico' "
        + "and log.endpoint not like '/css/%' "
        + "and log.endpoint not like '/js/%' "
        + "and log.endpoint not like '/images/%' "
        + "and log.endpoint not like '/webjars/%'")
    long countRelevantSuccessfulRequestsBetween(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to,
                                               @Param("min") int minStatus,
                                               @Param("max") int maxStatus);

    default long countMeaningfulFailedRequestsBetween(LocalDateTime from, LocalDateTime to) {
        return countMeaningfulFailedRequestsBetween(from, to, 400);
    }

    @Query("select count(log) from FluxGuardRequestLog log "
        + "where log.timestamp >= :from and log.timestamp < :to "
        + "and log.responseStatus >= :status "
        + "and log.endpoint not like '/favicon.ico' "
        + "and log.endpoint not like '/css/%' "
        + "and log.endpoint not like '/js/%' "
        + "and log.endpoint not like '/images/%' "
        + "and log.endpoint not like '/webjars/%'")
    long countMeaningfulFailedRequestsBetween(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             @Param("status") int status);

    default List<FluxGuardRequestLog> findTop100MeaningfulFailuresBetween(LocalDateTime from, LocalDateTime to) {
        return findTop100MeaningfulFailuresBetween(from, to, 400);
    }

    @Query("select log from FluxGuardRequestLog log "
        + "where log.timestamp >= :from and log.timestamp < :to "
        + "and log.responseStatus >= :status "
        + "and log.endpoint not like '/favicon.ico' "
        + "and log.endpoint not like '/css/%' "
        + "and log.endpoint not like '/js/%' "
        + "and log.endpoint not like '/images/%' "
        + "and log.endpoint not like '/webjars/%' "
        + "order by log.timestamp desc")
    List<FluxGuardRequestLog> findTop100MeaningfulFailuresBetween(@Param("from") LocalDateTime from,
                                                                  @Param("to") LocalDateTime to,
                                                                  @Param("status") int status);

    @Query("select count(distinct log.sessionId) from FluxGuardRequestLog log "
        + "where log.timestamp >= :since and log.userId is not null "
        + "and log.eventType = 'REQUEST' and log.sessionId is not null and log.sessionId <> 'none'")
    long countActiveSessionsSince(@Param("since") LocalDateTime since);

    @Query("select log.instanceId as instanceId, count(distinct log.sessionId) as activeUsers "
        + "from FluxGuardRequestLog log where log.timestamp >= :since "
        + "and log.userId is not null and log.eventType = 'REQUEST' "
        + "and log.sessionId is not null and log.sessionId <> 'none' and log.instanceId is not null "
        + "group by log.instanceId order by log.instanceId")
    List<InstanceUserCount> findInstanceUserCountsSince(@Param("since") LocalDateTime since);

    List<FluxGuardRequestLog> findByUserIdAndEventTypeOrderByTimestampDesc(Long userId, String eventType);

    interface InstanceUserCount {
        String getInstanceId();
        long getActiveUsers();
    }
}
