package com.lazar.gymaccess.model.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="members")
public class Member {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private LocalDate membershipExpiresAt;

    @Column(name = "qr_token_hash", length = 64)
    private String qrTokenHash;

    @Column(name = "qr_token_created_at")
    private Instant qrTokenCreatedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Member() {

    }

    public Member(String firstName, String lastName, String email, LocalDate membershipExpiresAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.membershipExpiresAt = membershipExpiresAt;
        this.status = MemberStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


    public boolean isMemberShipActive() {
        return status == MemberStatus.ACTIVE && !membershipExpiresAt.isBefore(LocalDate.now());
    }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    public void reactivate(LocalDate newExpiryDate) {
        this.membershipExpiresAt = newExpiryDate;
        this.status = MemberStatus.ACTIVE;
    }


    public void setFirstName(@NotBlank String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(@NotBlank String lastName) {
        this.lastName = lastName;
    }

    public void setQrTokenHash(String qrTokenHash, Instant createdAt) {
        this.qrTokenHash = qrTokenHash;
        this.qrTokenCreatedAt = createdAt;
    }

    public String getQrTokenHash() {
        return qrTokenHash;
    }

    public Instant getQrTokenCreatedAt() {
        return qrTokenCreatedAt;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public void setMembershipExpiresAt(LocalDate membershipExpiresAt) {
        this.membershipExpiresAt = membershipExpiresAt;
    }

    public UUID getId() {
        return id;
    }

    public @NotBlank String getFirstName() {
        return firstName;
    }

    public @NotBlank String getLastName() {
        return lastName;
    }

    public @Email @NotBlank String getEmail() {
        return email;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public LocalDate getMembershipExpiresAt() {
        return membershipExpiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
