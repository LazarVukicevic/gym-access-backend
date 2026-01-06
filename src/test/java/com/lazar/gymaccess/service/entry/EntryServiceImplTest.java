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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@ExtendWith(MockitoExtension.class)
public class EntryServiceImplTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    EntryLogRepository entryLogRepository;

    @InjectMocks
    EntryServiceImpl entryService;

    private UUID memberId;
    private Member member;
    private final String rawToken = "raw-qr-token";
    private final String hashedToken = QrTokenHasher.hash(rawToken);

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        member = new Member("Lazar", "Vukicevic", "lazar@example.com", LocalDate.now().plusYears(1));


//        setField(member, "id", memberId);
    }


    @Test
    void checkIn_deniesAndLogs_whenQrTokenInvalid() {
        CheckInRequest request = new CheckInRequest(rawToken, "front");

        when(memberRepository.findByQrTokenHash(hashedToken)).thenReturn(Optional.empty());

        CheckInResponse response = entryService.checkIn(request);

        assertThat(response.allowed()).isFalse();
        assertThat(response.memberName()).isNull();
        assertThat(response.message()).isEqualTo("Invalid QR code");

        EntryLog saved = captureSavedLog();

        assertThat(saved.getMemberId()).isNull();
        assertThat(saved.getGateId()).isEqualTo("front");
        assertThat(saved.getReason()).isEqualTo(EntryReason.INVALID_QR);
        assertThat(saved.getResult()).isEqualTo(EntryResult.DENIED);
    }


    @Test
    void checkIn_deniesAndLogs_whenSuspended() {
        CheckInRequest request = new CheckInRequest(rawToken, "front");
        setField(member, "status", MemberStatus.SUSPENDED);
        when(memberRepository.findByQrTokenHash(hashedToken)).thenReturn(Optional.of(member));

        CheckInResponse response = entryService.checkIn(request);

        assertThat(response.allowed()).isFalse();
        assertThat(response.memberName()).isEqualTo("Lazar Vukicevic");
        assertThat(response.message()).isEqualTo("Membership is suspended");

        EntryLog saved = captureSavedLog();

        assertThat(saved.getMemberId()).isEqualTo(member.getId());
        assertThat(saved.getGateId()).isEqualTo("front");
        assertThat(saved.getResult()).isEqualTo(EntryResult.DENIED);
        assertThat(saved.getReason()).isEqualTo(EntryReason.SUSPENDED);
    }

    @Test
    void checkIn_deniesAndLogs_whenExpired() {
        CheckInRequest request = new CheckInRequest(rawToken, "front");
        setField(member, "membershipExpiresAt", LocalDate.now().minusDays(1));
        setField(member, "status", MemberStatus.ACTIVE);
        when(memberRepository.findByQrTokenHash(hashedToken)).thenReturn(Optional.of(member));

        CheckInResponse response = entryService.checkIn(request);

        assertThat(response.allowed()).isFalse();
        assertThat(response.memberName()).isEqualTo("Lazar Vukicevic");
        assertThat(response.message()).isEqualTo("Membership is expired");

        EntryLog saved = captureSavedLog();

        assertThat(saved.getMemberId()).isEqualTo(member.getId());
        assertThat(saved.getGateId()).isEqualTo("front");
        assertThat(saved.getResult()).isEqualTo(EntryResult.DENIED);
        assertThat(saved.getReason()).isEqualTo(EntryReason.EXPIRED);
    }

    @Test
    void checkIn_allowsAndLogs_whenActiveAndNotExpired() {
        CheckInRequest request = new CheckInRequest(rawToken, "front");

        when(memberRepository.findByQrTokenHash(hashedToken)).thenReturn(Optional.of(member));

        CheckInResponse response = entryService.checkIn(request);

        assertThat(response.allowed()).isTrue();
        assertThat(response.memberName()).isEqualTo("Lazar Vukicevic");
        assertThat(response.message()).isEqualTo("Access granted");

        EntryLog saved = captureSavedLog();

        assertThat(saved.getMemberId()).isEqualTo(member.getId());
        assertThat(saved.getGateId()).isEqualTo("front");
        assertThat(saved.getResult()).isEqualTo(EntryResult.ALLOWED);
        assertThat(saved.getReason()).isNull();
    }

    @Test
    void checkIn_trimsGateId_andConvertsBlankToNull() {
        when(memberRepository.findByQrTokenHash(hashedToken)).thenReturn(Optional.of(member));

        CheckInResponse r1 = entryService.checkIn(new CheckInRequest(rawToken, "  front  "));
        assertThat(r1.allowed()).isTrue();

        EntryLog saved1 = captureSavedLog();
        assertThat(saved1.getGateId()).isEqualTo("front");

        reset(entryLogRepository);

        CheckInResponse r2 = entryService.checkIn(new CheckInRequest(rawToken, "   "));
        assertThat(r2.allowed()).isTrue();

        EntryLog saved2 = captureSavedLog();
        assertThat(saved2.getGateId()).isNull();
    }



    private EntryLog captureSavedLog() {
        ArgumentCaptor<EntryLog> captor = ArgumentCaptor.forClass(EntryLog.class);
        verify(entryLogRepository).save(captor.capture());
        return captor.getValue();
    }


}
