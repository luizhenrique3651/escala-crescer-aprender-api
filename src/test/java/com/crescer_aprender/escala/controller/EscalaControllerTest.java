package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.dto.EscalaCreateRequest;
import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.security.JwtService;
import com.crescer_aprender.escala.service.EscalaService;
import com.crescer_aprender.escala.service.UsuarioDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EscalaController.class)
@AutoConfigureMockMvc(addFilters = false)
class EscalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EscalaService service;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAll_Success() throws Exception {
        Escala e = Escala.builder().id(1L).mes(1).ano(2025L).build();
        when(service.loadAll()).thenReturn(Optional.of(List.of(e)));

        mockMvc.perform(get("/crescer-aprender/escala"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetById_Success() throws Exception {
        Escala e = Escala.builder().id(1L).build();
        when(service.findById(1L)).thenReturn(Optional.of(e));

        mockMvc.perform(get("/crescer-aprender/escala/byId/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/crescer-aprender/escala/byId/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate_Success() throws Exception {
        EscalaCreateRequest request = EscalaCreateRequest.builder().mes(1).ano(2025L).build();
        Escala saved = Escala.builder().id(1L).build();
        when(service.saveFromRequest(any(EscalaCreateRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/crescer-aprender/escala")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testUpdate_Success() throws Exception {
        Escala e = Escala.builder().mes(2).build();
        Escala updated = Escala.builder().id(1L).mes(2).build();
        when(service.update(eq(1L), any(Escala.class))).thenReturn(updated);

        mockMvc.perform(put("/crescer-aprender/escala/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(e)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value(2));
    }

    @Test
    void testDelete_Success() throws Exception {
        when(service.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/crescer-aprender/escala/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Escala", 1L)).when(service).delete(1L);

        mockMvc.perform(delete("/crescer-aprender/escala/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testPopulateVoluntarios_Success() throws Exception {
        Escala e = Escala.builder().id(1L).build();
        when(service.populaEscalaComVoluntarios(1L)).thenReturn(e);

        mockMvc.perform(put("/crescer-aprender/escala/popula-voluntarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testPopulateVoluntarios_Error() throws Exception {
        when(service.populaEscalaComVoluntarios(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(put("/crescer-aprender/escala/popula-voluntarios/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error"));
    }
}
