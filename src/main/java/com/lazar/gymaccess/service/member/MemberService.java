package com.lazar.gymaccess.service.member;

import com.lazar.gymaccess.dto.member.MemberCreateRequest;
import com.lazar.gymaccess.dto.member.MemberResponse;
import com.lazar.gymaccess.dto.member.MemberUpdateRequest;
import com.lazar.gymaccess.dto.member.QrTokenRotateResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface MemberService {

    MemberResponse createMember(MemberCreateRequest request);

    MemberResponse getMember(UUID memberId);

    List<MemberResponse> listMembers(String query, String status, int limit);

    MemberResponse updateMember(UUID memberId, MemberUpdateRequest request);

    MemberResponse suspendMember(UUID memberId);

    MemberResponse reactivateMember(UUID memberId, LocalDate newExpiryDate);

    void deleteMember(UUID memberId);

    QrTokenRotateResponse rotateQrToken(UUID memberId);
}
