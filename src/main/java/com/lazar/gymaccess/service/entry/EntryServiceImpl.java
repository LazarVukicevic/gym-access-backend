package com.lazar.gymaccess.service.entry;

import com.lazar.gymaccess.dto.entry.CheckInRequest;
import com.lazar.gymaccess.dto.entry.CheckInResponse;
import com.lazar.gymaccess.model.entry.EntryLog;
import com.lazar.gymaccess.model.entry.EntryReason;
import com.lazar.gymaccess.model.entry.EntryResult;
import com.lazar.gymaccess.model.member.Member;
import com.lazar.gymaccess.model.member.MemberStatus;
import com.lazar.gymaccess.repository.EntryLogRepository;
import com.lazar.gymaccess.repository.MemberRepository;
import com.lazar.gymaccess.util.QrTokenHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EntryServiceImpl implements EntryService {

    private final MemberRepository memberRepository;
    private final EntryLogRepository entryLogRepository;

    public EntryServiceImpl(MemberRepository memberRepository, EntryLogRepository entryLogRepository) {
        this.memberRepository = memberRepository;
        this.entryLogRepository = entryLogRepository;
    }

    @Override
    public CheckInResponse checkIn(CheckInRequest request) {
        String gateId = normalizeGateId(request.gateId());

        String tokenHash;
        try {
            tokenHash = QrTokenHasher.hash(request.qrToken());
        } catch (IllegalArgumentException e) {
            saveLog(null, EntryResult.DENIED, EntryReason.INVALID_QR, gateId);
            return new CheckInResponse(false, "Invalid QR code", null);
        }

        System.out.println(tokenHash);
        Member member = memberRepository.findByQrTokenHash(tokenHash).orElse(null);
        System.out.println(member);

        if (member == null) {
            saveLog(null, EntryResult.DENIED, EntryReason.INVALID_QR, gateId);
            return new CheckInResponse(false, "Invalid QR code", null);
        }

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            saveLog(member.getId(), EntryResult.DENIED, EntryReason.SUSPENDED, gateId);
            return new CheckInResponse(false, "Membership is suspended", fullName(member));
        }

        if (isExpired(member) || member.getStatus() == MemberStatus.EXPIRED) {
            saveLog(member.getId(), EntryResult.DENIED, EntryReason.EXPIRED, gateId);
            return new CheckInResponse(false, "Membership is expired", fullName(member));
        }

        saveLog(member.getId(), EntryResult.ALLOWED, null, gateId);
        return new CheckInResponse(true, "Access granted", fullName(member));
    }

    @Override
    public List<EntryLog> recent(int minutes, int limit) {
        Instant since = Instant.now().minus(minutes, ChronoUnit.MINUTES);
        return entryLogRepository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(since)
                .stream().limit(limit).toList();
    }

    @Override
    public List<EntryLog> byMember(UUID memberId, int limit) {
        return entryLogRepository.findByMemberIdOrderByOccurredAtDesc(memberId)
                .stream().limit(limit).toList();
    }


    private void saveLog(UUID memberId, EntryResult result, EntryReason reason, String gateId) {
        EntryLog log = new EntryLog(memberId, result, reason, gateId);
        entryLogRepository.save(log);
    }

    private boolean isExpired(Member member) {
        if (member.getMembershipExpiresAt() == null) {
            return true;
        }
        return member.getMembershipExpiresAt().isBefore(LocalDate.now());
    }

    private String fullName(Member member) {
        String first = member.getFirstName() == null ? "" : member.getFirstName().trim();
        String last = member.getLastName() == null ? "" : member.getLastName().trim();
        String name = (first + " " + last).trim();
        return name.isBlank() ? null : name;
    }

    private String normalizeGateId(String gateId) {
        if (gateId == null) {
            return null;
        }
        String trimmed = gateId.trim();
        return trimmed.isBlank() ? null : trimmed;
    }


}
