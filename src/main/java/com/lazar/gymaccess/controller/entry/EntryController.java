package com.lazar.gymaccess.controller.entry;


import com.lazar.gymaccess.dto.entry.CheckInRequest;
import com.lazar.gymaccess.dto.entry.CheckInResponse;
import com.lazar.gymaccess.dto.entry.EntryLogResponse;
import com.lazar.gymaccess.model.entry.EntryLog;
import com.lazar.gymaccess.service.entry.EntryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entries")
@Validated
public class EntryController {

    private final EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(entryService.checkIn(request));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<EntryLogResponse>> recent(
            @RequestParam(name = "minutes", defaultValue = "60") @Min(1) @Max(1440) int minutes,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        List<EntryLog> logs = entryService.recent(minutes, limit);
        return ResponseEntity.ok(logs.stream().map(EntryLogResponse::from).toList());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<EntryLogResponse>> memberHistory(
            @PathVariable UUID memberId,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit
    ) {
        List<EntryLog> logs = entryService.byMember(memberId, limit);
        return ResponseEntity.ok(logs.stream().map(EntryLogResponse::from).toList());
    }



}
