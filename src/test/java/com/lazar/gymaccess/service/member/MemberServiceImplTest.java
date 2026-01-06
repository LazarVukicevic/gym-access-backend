package com.lazar.gymaccess.service.member;


import com.lazar.gymaccess.dto.member.MemberCreateRequest;
import com.lazar.gymaccess.dto.member.MemberResponse;
import com.lazar.gymaccess.dto.member.MemberUpdateRequest;
import com.lazar.gymaccess.dto.member.QrTokenRotateResponse;
import com.lazar.gymaccess.model.member.Member;
import com.lazar.gymaccess.model.member.MemberStatus;
import com.lazar.gymaccess.repository.MemberRepository;
import com.lazar.gymaccess.util.QrTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private UUID memberId;
    private Member existingMember;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        existingMember = new Member(
                "Lazar",
                "Vuk",
                "LazVuk@example.com",
                LocalDate.now().plusYears(1)
        );
    }

    @Test
    void rotateQrToken_setsNewHashAndTimeStamp_andReturnsRawToken() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        QrTokenRotateResponse response = memberService.rotateQrToken(memberId);
        assertThat(response.qrToken()).isNotBlank();

        assertThat(existingMember.getQrTokenCreatedAt()).isNotNull();
        assertThat(existingMember.getQrTokenHash()).isEqualTo(QrTokenHasher.hash(response.qrToken()));
        verify(memberRepository).save(existingMember);
    }

    @Test
    void rotateQrToken_throwsNotFound_whenMemberMissing() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(()->memberService.rotateQrToken(memberId));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void createMember_createsMember_whenEmailNotInUse() {
        MemberCreateRequest req = new MemberCreateRequest("John", "Doe", "john@example.com", LocalDate.now().plusDays(90));

        when(memberRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.empty());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = memberService.createMember(req);

        verify(memberRepository).findByEmailIgnoreCase("john@example.com");
        verify(memberRepository).save(captor.capture());

        Member saved = captor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(saved.getMembershipExpiresAt()).isEqualTo(req.membershipExpiresAt());

        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void createMember_throwsConflict_whenEmailAlreadyInUse() {
//        MemberCreateRequest req = createReq("John", "Doe", "john@example.com", LocalDate.now().plusYears(1));
        MemberCreateRequest req = new MemberCreateRequest("John", "Doe", "john@example.com", LocalDate.now().plusDays(90));

        when(memberRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(existingMember));

        Throwable thrown = catchThrowable(()->memberService.createMember(req));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        verify(memberRepository, never()).save(any());

    }

    @Test
    void getMember_returnsMember_whenFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        var response = memberService.getMember(memberId);

        assertThat(response.email()).isEqualTo(existingMember.getEmail());
        verify(memberRepository).findById(memberId);
    }

    @Test
    void getMember_throwsNotFound_whenMissing() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(()->memberService.getMember(memberId));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listMembers_usesRepositorySearch_andLimitsResults() {
        Member m1 = new Member("A", "A", "a@example.com", LocalDate.now().plusDays(10));
        Member m2 = new Member("B", "B", "b@example.com", LocalDate.now().plusDays(10));
        Member m3 = new Member("C", "C", "c@example.com", LocalDate.now().plusDays(10));

        when(memberRepository.searchMembers(eq("la"), eq(MemberStatus.ACTIVE)))
                .thenReturn(List.of(m1, m2, m3));
        var responses = memberService.listMembers("la", "ACTIVE", 2);

        assertThat(responses).hasSize(2);
        verify(memberRepository).searchMembers("la", MemberStatus.ACTIVE);
    }

    @Test
    void listMembers_throwsBadRequest_forInvalidStatus() {
        Throwable thrown = catchThrowable(()->memberService.listMembers("x", "NOT_A_REAL_STATUS", 10));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateMember_updatesOnlyProvidedFields() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        MemberUpdateRequest req = new MemberUpdateRequest(null, null, null, LocalDate.now().plusDays(90));

//        setField(req, "membershipExpiresAt", LocalDate.now().plusDays(90));

        MemberResponse response = memberService.updateMember(memberId, req);

        assertThat(response.membershipExpiresAt()).isEqualTo(LocalDate.now().plusDays(90));
        assertThat(response.firstName()).isEqualTo("Lazar");
        assertThat(response.email()).isEqualTo("LazVuk@example.com");

        verify(memberRepository, never()).findByEmailIgnoreCase(anyString());

    }

    @Test
    void updateMember_checksUniqueness_whenChangingEmail() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        MemberUpdateRequest req = new MemberUpdateRequest(null, null, "newEmail@example.com", null);

//        setField(req, "email", "newEmail@example.com");

        when(memberRepository.findByEmailIgnoreCase("newEmail@example.com")).thenReturn(Optional.empty());
        MemberResponse response = memberService.updateMember(memberId, req);

        verify(memberRepository).findByEmailIgnoreCase("newEmail@example.com");
        assertThat(response.email()).isEqualTo("newEmail@example.com");
    }

    @Test
    void updateMember_throwsConflict_whenNewEmailInUseByAnotherMember() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        MemberUpdateRequest req = new MemberUpdateRequest(null, null, "taken@example.com", null);
//        setField(req, "email", "taken@example.com");

        Member other = new Member("Other", "Person", "taken@example.com", LocalDate.now().plusDays(90));
        when(memberRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(other));


        Throwable thrown = catchThrowable(()->memberService.updateMember(memberId, req));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void suspendMember_setsStatusSuspended() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        MemberResponse response = memberService.suspendMember(memberId);

        assertThat(response.status()).isEqualTo(MemberStatus.SUSPENDED);
        assertThat(existingMember.isMemberShipActive()).isEqualTo(false);
        assertThat(existingMember.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    void reactivateMember_setsStatusActive_andUpdatesExpiry() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));
        LocalDate newExpiry = LocalDate.now().plusDays(120);

        MemberResponse response = memberService.reactivateMember(memberId, newExpiry);

        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(response.membershipExpiresAt()).isEqualTo(newExpiry);
        assertThat(existingMember.isMemberShipActive()).isEqualTo(true);
        assertThat(existingMember.getMembershipExpiresAt()).isEqualTo(newExpiry);
    }

    @Test
    void deleteMember_deletes_whenExists() {
        when(memberRepository.existsById(memberId)).thenReturn(true);
        memberService.deleteMember(memberId);
        verify(memberRepository).deleteById(memberId);
    }

    @Test
    void deleteMember_throwsNotFound_whenMissing() {
        when(memberRepository.existsById(memberId)).thenReturn(false);

        Throwable thrown = catchThrowable(()->memberService.deleteMember(memberId));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException e = (ResponseStatusException) thrown;
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(memberRepository, never()).deleteById(any());
    }






//
//    private MemberCreateRequest createReq(String first, String last, String email, LocalDate expiry) {
//        MemberCreateRequest req = new MemberCreateRequest();
//        setField(req, "firstName", first);
//        setField(req, "lastName", last);
//        setField(req, "email", email);
//        setField(req, "membershipExpiresAt", expiry);
//        return req;
//    }

}
