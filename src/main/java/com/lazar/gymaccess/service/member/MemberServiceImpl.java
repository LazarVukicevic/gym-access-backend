package com.lazar.gymaccess.service.member;

import com.lazar.gymaccess.dto.member.MemberCreateRequest;
import com.lazar.gymaccess.dto.member.MemberResponse;
import com.lazar.gymaccess.dto.member.MemberUpdateRequest;
import com.lazar.gymaccess.dto.member.QrTokenRotateResponse;
import com.lazar.gymaccess.model.member.Member;
import com.lazar.gymaccess.model.member.MemberStatus;
import com.lazar.gymaccess.repository.MemberRepository;
import com.lazar.gymaccess.util.QrTokenHasher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Override
    public MemberResponse createMember(MemberCreateRequest request) {
        String email = request.email().trim();

        memberRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use"); // 409
        });

        Member member = new Member(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                request.membershipExpiresAt()
        );

        Member saved = memberRepository.save(member);
        return toResponse(saved);
    }



    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Memebr not found"));
        return toResponse(member);
    }

    @Override
    public List<MemberResponse> listMembers(String query, String status, int limit) {
        MemberStatus parsedStatus = parseStatusOrNull(status);

        List<Member> results = memberRepository.searchMembers(
                isBlank(query) ? null : query.trim(),
                parsedStatus
        );

        return results.stream().limit(Math.max(1, limit)).map(this::toResponse).toList();
    }

    @Override
    public MemberResponse updateMember(UUID memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        String newFirstName = request.firstName();
        String newLastName = request.lastName();
        if (newFirstName != null) {
            member.setFirstName(newFirstName);
        }
        if (newLastName != null) {
            member.setLastName(newLastName);
        }


        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            memberRepository.findByEmailIgnoreCase(newEmail).ifPresent(existing -> {
                if (!Objects.equals(existing.getId(), memberId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                }
            });
            member.setEmail(newEmail);
        }

        if (request.membershipExpiresAt() != null) {
            member.setMembershipExpiresAt(request.membershipExpiresAt());
        }

        return toResponse(member);
    }

    @Override
    public MemberResponse suspendMember(UUID memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found."));
        member.suspend();
        return toResponse(member);
    }

    @Override
    public MemberResponse reactivateMember(UUID memberId, LocalDate newExpiryDate) {
        if (newExpiryDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newExpiryDate is required");
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found."));
        member.reactivate(newExpiryDate);
        return toResponse(member);
    }

    @Override
    public void deleteMember(UUID memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }

        memberRepository.deleteById(memberId);
    }

    @Override
    public QrTokenRotateResponse rotateQrToken(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = QrTokenHasher.hash(rawToken);

        member.setQrTokenHash(tokenHash, Instant.now());
        memberRepository.save(member);
        return new QrTokenRotateResponse(rawToken);
    }

    private MemberResponse toResponse(Member m) {
        return new MemberResponse(
                m.getId(),
                m.getFirstName(),
                m.getLastName(),
                m.getEmail(),
                m.getStatus(),
                m.getMembershipExpiresAt(),
                m.getUpdatedAt(),
                m.getUpdatedAt()
        );
    }

    private MemberStatus parseStatusOrNull(String status) {
        if (isBlank(status)) {
            return null;
        }
        try {
            return MemberStatus.valueOf(status.trim().toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }



}
