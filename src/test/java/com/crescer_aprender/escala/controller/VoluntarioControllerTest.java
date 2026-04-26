package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.VoluntarioIsScheduledException;
import com.crescer_aprender.escala.security.JwtService;
import com.crescer_aprender.escala.service.UsuarioDetailsService;
import com.crescer_aprender.escala.service.VoluntarioService;
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

@WebMvcTest(VoluntarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class VoluntarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoluntarioService service;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllVoluntarios_Success() throws Exception {
        Voluntario v = Voluntario.builder().id(1L).nome("Test").build();
        when(service.loadAll()).thenReturn(Optional.of(List.of(v)));

        mockMvc.perform(get("/crescer-aprender/voluntarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Test"));
    }

    @Test
    void testGetAllVoluntarios_NoContent() throws Exception {
        when(service.loadAll()).thenReturn(Optional.empty());

        mockMvc.perform(get("/crescer-aprender/voluntarios"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSave_Success() throws Exception {
        Voluntario v = Voluntario.builder().nome("Test").build();
        Voluntario saved = Voluntario.builder().id(1L).nome("Test").build();
        when(service.save(any(Voluntario.class))).thenReturn(saved);

        mockMvc.perform(post("/crescer-aprender/voluntarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testSave_EmailAlreadyExists() throws Exception {
        Voluntario v = Voluntario.builder().nome("Test").build();
        when(service.save(any(Voluntario.class))).thenThrow(new EmailAlreadyExistsException("test@test.com"));

        mockMvc.perform(post("/crescer-aprender/voluntarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testUpdate_Success() throws Exception {
        Voluntario v = Voluntario.builder().nome("Updated").build();
        Voluntario updated = Voluntario.builder().id(1L).nome("Updated").build();
        when(service.update(eq(1L), any(Voluntario.class))).thenReturn(updated);

        mockMvc.perform(put("/crescer-aprender/voluntarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Updated"));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Voluntario v = Voluntario.builder().nome("Updated").build();
        when(service.update(eq(1L), any(Voluntario.class))).thenThrow(new EntityNotFoundException("Voluntario", 1L));

        mockMvc.perform(put("/crescer-aprender/voluntarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_Success() throws Exception {
        when(service.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/crescer-aprender/voluntarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_Scheduled() throws Exception {
        doThrow(new VoluntarioIsScheduledException()).when(service).delete(1L);

        mockMvc.perform(delete("/crescer-aprender/voluntarios/1"))
                .andExpect(status().isUnprocessableEntity());
    }
}
