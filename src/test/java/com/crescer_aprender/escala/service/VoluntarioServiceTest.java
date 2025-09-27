package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.InvalidVoluntarioDataException;
import com.crescer_aprender.escala.exception.VoluntarioIsScheduledException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VoluntarioServiceTest {

    @Mock
    private VoluntarioRepository voluntarioRepository;

    @Mock
    private EscalaRepository escalaRepository;

    @InjectMocks
    private VoluntarioService voluntarioService;

    private Voluntario voluntario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        voluntario = Voluntario.builder()
                .id(1L)
                .nome("Maria")
                .datasDisponiveis(new ArrayList<>(Arrays.asList(
                        LocalDate.of(2025, 8, 2),
                        LocalDate.of(2025, 8, 9)
                )))
                .build();
    }

    @Test
    void save_Sucesso() {
        when(voluntarioRepository.save(any())).thenReturn(voluntario);

        Voluntario saved = voluntarioService.save(voluntario);

        assertNotNull(saved);
        assertEquals("Maria", saved.getNome());
        verify(voluntarioRepository).save(voluntario);
    }

    @Test
    void save_FalhaNomeNulo() {
        voluntario.setNome(null);

        InvalidVoluntarioDataException e = assertThrows(InvalidVoluntarioDataException.class, () -> {
            voluntarioService.save(voluntario);
        });

        assertEquals("O nome do voluntário é obrigatório.", e.getMessage());
    }

//    @Test
//    void save_FalhaEmailInvalido() {
//        voluntario.setEmail("invalidemail");
//
//        InvalidVoluntarioDataException e = assertThrows(InvalidVoluntarioDataException.class, () -> {
//            voluntarioService.save(voluntario);
//        });
//
//        assertEquals("O e-mail do voluntário é inválido.", e.getMessage());
//    }

//    @Test
//    void save_FalhaEmailJaExiste() {
//        when(voluntarioRepository.existsByEmail(voluntario.getEmail())).thenReturn(true);
//
//        EmailAlreadyExistsException e = assertThrows(EmailAlreadyExistsException.class, () -> {
//            voluntarioService.save(voluntario);
//        });
//
//        assertEquals("O e-mail maria@email.com já está em uso.", e.getMessage());
//    }

    @Test
    void loadAll_RetornaLista() {
        List<Voluntario> lista = List.of(voluntario);
        when(voluntarioRepository.findAll()).thenReturn(lista);

        Optional<List<Voluntario>> result = voluntarioService.loadAll();

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
    }

    @Test
    void update_Sucesso() {
        Voluntario updateVoluntario = Voluntario.builder()
                .nome("Maria Atualizada")
                .datasDisponiveis(new ArrayList<>(Arrays.asList(LocalDate.of(2025, 8, 16))))
                .build();

        when(voluntarioRepository.findById(1L)).thenReturn(Optional.of(voluntario));
        when(escalaRepository.findByVoluntario(any())).thenReturn(Optional.empty());
        when(voluntarioRepository.save(any())).thenReturn(voluntario);

        Voluntario result = voluntarioService.update(1L, updateVoluntario);

        assertEquals("Maria Atualizada", result.getNome());
        assertTrue(result.getDatasDisponiveis().contains(LocalDate.of(2025, 8, 16)));
    }

    @Test
    void update_FalhaVoluntarioNaoEncontrado() {
        when(voluntarioRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> {
            voluntarioService.update(99L, voluntario);
        });

        assertTrue(e.getMessage().contains("Voluntário com ID 99 não encontrado"));
    }

    @Test
    void delete_Sucesso() {
        when(voluntarioRepository.existsById(1L)).thenReturn(true);
        when(escalaRepository.existsByVoluntarios(any())).thenReturn(false);
        doNothing().when(voluntarioRepository).deleteById(1L);

        assertTrue(voluntarioService.delete(1L));
        verify(voluntarioRepository).deleteById(1L);
    }

    @Test
    void delete_FalhaVoluntarioEmEscala() {
        when(voluntarioRepository.existsById(1L)).thenReturn(true);
        when(escalaRepository.existsByVoluntarios(any())).thenReturn(true);

        VoluntarioIsScheduledException e = assertThrows(VoluntarioIsScheduledException.class, () -> {
            voluntarioService.delete(1L);
        });

        assertEquals("Voluntário não pode ser deletado pois está escalado", e.getMessage());
    }

    @Test
    void delete_FalhaVoluntarioNaoEncontrado() {
        when(voluntarioRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> {
            voluntarioService.delete(99L);
        });

        assertTrue(e.getMessage().contains("Voluntário com ID 99 não encontrado"));
    }
}
