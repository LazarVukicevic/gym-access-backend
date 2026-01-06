package com.lazar.gymaccess.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazar.gymaccess.dto.member.MemberCreateRequest;
import com.lazar.gymaccess.dto.member.MemberResponse;
import com.lazar.gymaccess.dto.member.QrTokenRotateResponse;
import com.lazar.gymaccess.model.member.MemberStatus;
import com.lazar.gymaccess.service.member.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberService memberService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_returns201_andBody_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        MemberCreateRequest req = new MemberCreateRequest(
                "John", "Doe", "john@example.com", LocalDate.now().plusDays(30));

        MemberResponse serviceResponse = new MemberResponse(id, "John", "Doe",
                "john@example.com", MemberStatus.ACTIVE, req.membershipExpiresAt(), now, now);
        when(memberService.createMember(any(MemberCreateRequest.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/v1/members").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(memberService).createMember(any(MemberCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_returns400_whenInvalidBody() throws Exception {
        String badJson = """
                {
                    "firstName": "",
                    "lastName": "Doe",
                    "email": "invalid_email",
                    "membershipExpiresAt": "1999-01-01"
                }
                """;
        mockMvc.perform(post("/api/v1/members").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
                .andExpect(status().isBadRequest());
        verify(memberService, never()).createMember(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMember_returns200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        MemberResponse serviceResponse = new MemberResponse(id, "Lazar", "Vuk", "lazar@example.com",
                MemberStatus.ACTIVE, LocalDate.now().plusDays(30), now, now);

        when(memberService.getMember(id)).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/v1/members/{memberId}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Lazar"))
                .andExpect(jsonPath("$.email").value("lazar@example.com"));

        verify(memberService).getMember(id);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void listMembers_passesQueryParams_toService() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Instant now = Instant.now();

        List<MemberResponse> response = List.of(
                new MemberResponse(id1, "A", "A", "A@example.com", MemberStatus.ACTIVE, LocalDate.now().plusDays(30), now, now),
                new MemberResponse(id2, "B", "B", "B@example.com", MemberStatus.SUSPENDED, LocalDate.now().plusDays(30), now, now)
        );

        when(memberService.listMembers("la", "ACTIVE", 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/members").with(csrf())
                .param("query", "la")
                .param("status", "ACTIVE")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[1].id").value(id2.toString()));
        verify(memberService).listMembers("la", "ACTIVE", 10);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMember_returns200() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(memberService).deleteMember(id);

        mockMvc.perform(delete("/api/v1/members/{memberId}", id).with(csrf()))
                .andExpect(status().isNoContent());
        verify(memberService).deleteMember(id);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void rotateQr_returns200_andToken() throws Exception {
        UUID memberId = UUID.randomUUID();

        when(memberService.rotateQrToken(memberId)).thenReturn(new QrTokenRotateResponse("raw-token-value"));

        mockMvc.perform(post("/api/v1/members/{memberId}/qr/rotate", memberId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.qrToken").value("raw-token-value"));
        verify(memberService).rotateQrToken(memberId);
    }



}
