package com.lazar.gymaccess.dto.entry;

import com.lazar.gymaccess.model.entry.EntryLog;
import com.lazar.gymaccess.model.entry.EntryReason;
import com.lazar.gymaccess.model.entry.EntryResult;

import java.time.Instant;
import java.util.UUID;

public record EntryLogResponse(
        UUID id,
        UUID memberId,
        Instant occurredAt,
        EntryResult result,
        EntryReason reason,
        String gateId
) {
    public static EntryLogResponse from(EntryLog log) {

        return new EntryLogResponse(
                log.getId(),
                log.getMemberId(),
                log.getOccurredAt(),
                log.getResult(),
                log.getReason(),
                log.getGateId()
        );
    }
}
