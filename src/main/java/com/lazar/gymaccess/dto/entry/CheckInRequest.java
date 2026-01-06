package com.lazar.gymaccess.dto.entry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CheckInRequest(
//        @NotNull(message = "memberId is required")UUID memberId,
        @NotBlank(message = "qrToken is required") String qrToken,
        @Size(max = 50, message = "gateId must be less than 50 characters") String gateId
) { }
