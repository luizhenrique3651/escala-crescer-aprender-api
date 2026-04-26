package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.security.JwtService;
import com.crescer_aprender.escala.service.UsuarioDetailsService;
import com.crescer_aprender.escala.service.UsuarioService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService service;

    @MockBean
    private VoluntarioService voluntarioService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSave_Success() throws Exception {
        Voluntario v = Voluntario.builder().nome("Test").build();
        Voluntario saved = Voluntario.builder().id(1L).nome("Test").build();
        when(voluntarioService.save(any(Voluntario.class))).thenReturn(saved);

        mockMvc.perform(post("/crescer-aprender/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testSave_Error() throws Exception {
        Voluntario v = Voluntario.builder().nome("Test").build();
        when(voluntarioService.save(any(Voluntario.class))).thenThrow(new EmailAlreadyExistsException("test@test.com"));

        mockMvc.perform(post("/crescer-aprender/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testGetAll_Success() throws Exception {
        Usuario u = Usuario.builder().id(1L).email("test@test.com").build();
        when(service.loadAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/crescer-aprender/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetAll_NoContent() throws Exception {
        when(service.loadAll()).thenReturn(List.of());

        mockMvc.perform(get("/crescer-aprender/usuarios"))
                .andExpect(status().isNoContent());
    }
}
