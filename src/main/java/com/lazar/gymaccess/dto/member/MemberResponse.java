//package com.lazar.gymaccess.dto.member;
//
//import com.lazar.gymaccess.model.member.MemberStatus;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.UUID;
//
//public class MemberResponse {
//    private UUID id;
//    private String firstName;
//    private String lastName;
//    private String email;
//    private MemberStatus status;
//    private LocalDate membershipExpiresAt;
//    private Instant createdAt;
//    private Instant updatedAt;
//
//    public MemberResponse(UUID id, String firstName, String lastName, String email, MemberStatus status,
//                          LocalDate membershipExpiresAt, Instant createdAt, Instant updatedAt) {
//        this.id = id;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.status = status;
//        this.membershipExpiresAt = membershipExpiresAt;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
//    }
//
//    public UUID getId() {
//        return id;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public MemberStatus getStatus() {
//        return status;
//    }
//
//    public LocalDate getMembershipExpiresAt() {
//        return membershipExpiresAt;
//    }
//
//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public Instant getUpdatedAt() {
//        return updatedAt;
//    }
//}
package com.lazar.gymaccess.dto.member;
import com.lazar.gymaccess.model.member.MemberStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        MemberStatus status,
        LocalDate membershipExpiresAt,
        Instant createdAt,
        Instant updatedAt
) { }