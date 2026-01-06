package com.lazar.gymaccess.controller.member;


import com.lazar.gymaccess.dto.member.MemberCreateRequest;
import com.lazar.gymaccess.dto.member.MemberResponse;
import com.lazar.gymaccess.dto.member.MemberUpdateRequest;
import com.lazar.gymaccess.dto.member.QrTokenRotateResponse;
import com.lazar.gymaccess.service.member.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@Validated
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberCreateRequest request) {
        MemberResponse created = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{memberId}/qr/rotate")
    public ResponseEntity<QrTokenRotateResponse> rotateQr(@PathVariable UUID memberId) {
        return ResponseEntity.ok(memberService.rotateQrToken(memberId));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable UUID memberId) {
        return ResponseEntity.ok(memberService.getMember(memberId));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> listMembers(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) int limit
    ) {
        return ResponseEntity.ok(memberService.listMembers(query, status, limit));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable UUID memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateMember(memberId, request));
    }

    @PostMapping("/{memberId}/suspend")
    @ResponseStatus(HttpStatus.OK)
    public MemberResponse suspendMember(@PathVariable UUID memberId) {
        return memberService.suspendMember(memberId);
    }

    @PostMapping("/{memberId}/reactivate")
    public ResponseEntity<MemberResponse> reactivateMember(
            @PathVariable UUID memberId,
            @RequestParam(name = "membershipExpiresAt")LocalDate membershipExpiresAt
    ) {
        return ResponseEntity.ok(memberService.reactivateMember(memberId, membershipExpiresAt));
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable UUID memberId) {
        memberService.deleteMember(memberId);
    }







}
