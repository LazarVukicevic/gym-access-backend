//package com.lazar.gymaccess.dto.member;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Future;
//
//import java.time.LocalDate;
//
//public class MemberUpdateRequest {
//    private String firstName;
//    private String lastName;
//
//    @Email(message = "Email must be valid")
//    private String email;
//
//    @Future(message = "Membership expiry date must be in the future")
//    private LocalDate membershipExpiresAt;
//
//    public MemberUpdateRequest() {}
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public @Email(message = "Email must be valid") String getEmail() {
//        return email;
//    }
//
//    public @Future(message = "Membership expiry date must be in the future") LocalDate getMembershipExpiresAt() {
//        return membershipExpiresAt;
//    }
//}

package com.lazar.gymaccess.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;

public record MemberUpdateRequest(
        String firstName,
        String lastName,
        @Email(message = "Email must be valid") String email,
        @Future(message = "Membership expiry date must be in the future")LocalDate membershipExpiresAt
) {}