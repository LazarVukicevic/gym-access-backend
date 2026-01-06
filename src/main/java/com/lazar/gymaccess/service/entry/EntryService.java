package com.lazar.gymaccess.service.entry;

import com.lazar.gymaccess.dto.entry.CheckInRequest;
import com.lazar.gymaccess.dto.entry.CheckInResponse;
import com.lazar.gymaccess.model.entry.EntryLog;

import java.util.List;
import java.util.UUID;

public interface EntryService {
    CheckInResponse checkIn(CheckInRequest request);
    List<EntryLog> recent(int minutes, int limit);
    List<EntryLog> byMember(UUID memberId, int limit);
}
