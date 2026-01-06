package com.lazar.gymaccess.model.entry;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entry_logs",
indexes = {
        @Index(name = "idx_entry_logs_occurred_at", columnList = "occurredAt"),
        @Index(name = "idx_entry_logs_member_id", columnList = "memberId"),
        @Index(name = "idx_entry_logs_result", columnList = "result")
})
public class EntryLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryResult result;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EntryReason reason;

    @Column(length = 50)
    private String gateId;

    protected EntryLog() {

    }

    public EntryLog(UUID memberId, EntryResult result, EntryReason reason, String gateId) {
        this.memberId = memberId;
        this.result = result;
        this.reason = reason;
        this.gateId = gateId;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if(this.occurredAt == null) {
            this.occurredAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public EntryResult getResult() {
        return result;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getGateId() {
        return gateId;
    }

    public EntryReason getReason() {
        return reason;
    }

    public UUID getMemberId() {
        return memberId;
    }
}
