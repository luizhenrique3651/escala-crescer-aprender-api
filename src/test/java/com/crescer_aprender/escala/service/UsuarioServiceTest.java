package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave_Success() {
        Usuario u = Usuario.builder().email("test@test.com").senha("123").build();
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(repository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Usuario saved = service.save(u);
        assertNotNull(saved.getId());
        assertEquals("encoded", saved.getSenha());
    }

    @Test
    void testSave_EmailAlreadyExists() {
        Usuario u = Usuario.builder().email("test@test.com").build();
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(u));

        assertThrows(EmailAlreadyExistsException.class, () -> service.save(u));
    }

    @Test
    void testLoadAll() {
        when(repository.findAll()).thenReturn(List.of(new Usuario()));
        List<Usuario> list = service.loadAll();
        assertEquals(1, list.size());
    }

    @Test
    void testFindByEmail() {
        Usuario u = new Usuario();
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(u));
        Optional<Usuario> result = service.findByEmail("test@test.com");
        assertTrue(result.isPresent());
    }
}
