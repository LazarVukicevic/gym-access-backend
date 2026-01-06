package com.lazar.gymaccess.repository;

import com.lazar.gymaccess.model.entry.EntryLog;
import com.lazar.gymaccess.model.entry.EntryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EntryLogRepository extends JpaRepository<EntryLog, UUID> {
    List<EntryLog> findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(Instant since);

    List<EntryLog> findByMemberIdOrderByOccurredAtDesc(UUID memberId);

    List<EntryLog> findByGateIdAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(String gateId, Instant since);

    List<EntryLog> findByResultAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(EntryResult result, Instant since);

    @Query("""
        SELECT e from EntryLog e
        WHERE (:gateId IS NULL OR e.gateId = :gateId)
        AND (:result IS NULL OR e.result = :result)
        AND e.occurredAt >= :since
        ORDER BY e.occurredAt DESC
    """)
    List<EntryLog> findRecentFiltered(
            @Param("since") Instant since,
            @Param("gateId") String gateId,
            @Param("result") EntryResult result
    );


}
