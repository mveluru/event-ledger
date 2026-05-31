package com.ledger.gateway.controller;

import com.ledger.gateway.entity.EventEntity;
import com.ledger.gateway.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerMvcTest {

    private MockMvc mockMvc;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = Mockito.mock(EventService.class);
        EventController controller = new EventController(eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void postEvents_success() throws Exception {
        when(eventService.ingest(any(EventEntity.class))).thenReturn("EVENT_ACCEPTED");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"evt-1\",\"accountId\":\"acc-1\",\"type\":\"CREDIT\",\"amount\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("EVENT_ACCEPTED"));
    }

    @Test
    void postEvents_duplicate() throws Exception {
        when(eventService.ingest(any(EventEntity.class))).thenReturn("DUPLICATE_EVENT");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"evt-1\",\"accountId\":\"acc-1\",\"type\":\"CREDIT\",\"amount\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("DUPLICATE_EVENT"));
    }

    @Test
    void postEvents_invalidAmount() throws Exception {
        when(eventService.ingest(any(EventEntity.class))).thenReturn("ERROR_INVALID_AMOUNT");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"evt-1\",\"accountId\":\"acc-1\",\"type\":\"CREDIT\",\"amount\":-10.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR_INVALID_AMOUNT"));
    }

    @Test
    void postEvents_invalidType() throws Exception {
        when(eventService.ingest(any(EventEntity.class))).thenReturn("ERROR_INVALID_TYPE");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"evt-1\",\"accountId\":\"acc-1\",\"type\":\"INVALID\",\"amount\":10.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR_INVALID_TYPE"));
    }

    @Test
    void postEvents_serviceUnavailable() throws Exception {
        when(eventService.ingest(any(EventEntity.class))).thenReturn("SERVICE_UNAVAILABLE_FALLBACK");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"evt-1\",\"accountId\":\"acc-1\",\"type\":\"CREDIT\",\"amount\":10.0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("SERVICE_UNAVAILABLE_FALLBACK"));
    }
}
