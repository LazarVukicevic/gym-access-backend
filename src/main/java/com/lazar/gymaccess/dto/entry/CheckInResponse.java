package com.lazar.gymaccess.dto.entry;

public record CheckInResponse(
        boolean allowed,
        String message,
        String memberName
) { }
