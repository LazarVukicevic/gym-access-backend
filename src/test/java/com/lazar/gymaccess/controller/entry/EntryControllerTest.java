package com.lazar.gymaccess.controller.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazar.gymaccess.dto.entry.CheckInRequest;
import com.lazar.gymaccess.dto.entry.CheckInResponse;
import com.lazar.gymaccess.model.entry.EntryLog;
import com.lazar.gymaccess.model.entry.EntryReason;
import com.lazar.gymaccess.model.entry.EntryResult;
import com.lazar.gymaccess.service.entry.EntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EntryController.class)
public class EntryControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    EntryService entryService;

    @Test
    @WithMockUser(roles = "STAFF")
    void checkIn_returns200_andBody_whenValid() throws Exception {
        String rawToken = "hello";

        CheckInRequest request = new CheckInRequest(rawToken, "FRONT");

        when(entryService.checkIn(any(CheckInRequest.class)))
                .thenReturn(new CheckInResponse(
                        true,
                        "Access granted",
                        "Lazar Vukicevic"
                ));

        mockMvc.perform(post("/api/v1/entries/check-in").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.message").value("Access granted"))
                .andExpect(jsonPath("$.memberName").value("Lazar Vukicevic"));

        verify(entryService).checkIn(any(CheckInRequest.class));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void checkIn_returns400_whenQrTokenMissing() throws Exception {
        String badJson = """
                {
                    "gateId": "front"
                }
                """;

        mockMvc.perform(post("/api/v1/entries/check-in").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
                .andExpect(status().isBadRequest());

        verify(entryService, never()).checkIn(any());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void memberHistory_returns200_andMapsLogs() throws Exception {
        UUID memberId = UUID.randomUUID();

        UUID logId = UUID.randomUUID();
        Instant t = Instant.now();

        EntryLog log = new EntryLog(memberId, EntryResult.DENIED, EntryReason.SUSPENDED, "SIDE");
        setField(log, "id", logId);
        setField(log, "occurredAt", t);

        when(entryService.byMember(memberId, 50)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/entries/member/{memberId}", memberId).param("limit", "50").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(logId.toString()))
                .andExpect(jsonPath("$[0].memberId").value(memberId.toString()))
                .andExpect(jsonPath("$[0].occurredAt").value(t.toString()))
                .andExpect(jsonPath("$[0].result").value("DENIED"))
                .andExpect(jsonPath("$[0].reason").value("SUSPENDED"))
                .andExpect(jsonPath("$[0].gateId").value("SIDE"));

        verify(entryService).byMember(memberId, 50);


    }



    @Test
    void memberHistory_returns401_whenNotAuthenticated() throws Exception {
        UUID memberId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/entries/member/{memberId}", memberId))
                .andExpect(status().isUnauthorized());
    }








}
