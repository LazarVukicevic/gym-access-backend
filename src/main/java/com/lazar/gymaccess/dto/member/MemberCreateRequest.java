//package com.lazar.gymaccess.dto.member;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Future;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//
//import java.time.LocalDate;
//
//public class MemberCreateRequest {
//    @NotBlank(message = "First name is required")
//    private String firstName;
//
//    @NotBlank(message = "Last name is required")
//    private String lastName;
//
//    @Email(message = "Email must be valid")
//    @NotBlank(message = "Email is reqired")
//    private String email;
//
//    @NotNull(message = "Membership expiry date is required")
//    @Future(message = "Membership expiry date must be in the future")
//    private LocalDate membershipExpiresAt;
//
//    public MemberCreateRequest() {}
//
//    public @NotBlank(message = "First name is required") String getFirstName() {
//        return firstName;
//    }
//
//    public @NotBlank(message = "Last name is required") String getLastName() {
//        return lastName;
//    }
//
//    public @Email(message = "Email must be valid") @NotBlank(message = "Email is reqired") String getEmail() {
//        return email;
//    }
//
//    public @NotNull(message = "Membership expiry date is required") @Future(message = "Membership expiry date must be in the future") LocalDate getMembershipExpiresAt() {
//        return membershipExpiresAt;
//    }
//}

package com.lazar.gymaccess.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MemberCreateRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotNull(message = "Membership expiry date is required")
        @Future(message = "Membership expiry date must be in the future")
        LocalDate membershipExpiresAt
) { }

