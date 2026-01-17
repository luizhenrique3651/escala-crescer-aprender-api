package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.enums.PerfisUsuariosEnum;
import com.crescer_aprender.escala.exception.*;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import com.crescer_aprender.escala.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VoluntarioServiceTest {

    @Mock
    private VoluntarioRepository voluntarioRepository;

    @Mock
    private EscalaRepository escalaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
                .usuario(Usuario.builder().email("maria@gmail.com").senha("maria123").role(PerfisUsuariosEnum.VOLUNTARIO).build())
                .build();
    }

    @Test
    void save_Sucesso() {
        // Preparar mocks para salvar usuario e voluntario
        when(usuarioRepository.findByEmail("maria@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("maria123")).thenReturn("encoded-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(voluntarioRepository.save(any(Voluntario.class))).thenAnswer(inv -> {
            Voluntario v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        Voluntario saved = voluntarioService.save(voluntario);

        assertNotNull(saved);
        assertEquals("Maria", saved.getNome());
        assertNotNull(saved.getUsuario());
        assertEquals(2L, saved.getUsuario().getId());
        // senha deve ter sido codificada
        assertEquals("encoded-senha", saved.getUsuario().getSenha());

        verify(usuarioRepository).findByEmail("maria@gmail.com");
        verify(passwordEncoder).encode("maria123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(voluntarioRepository).save(any(Voluntario.class));
    }

    @Test
    void save_FalhaNomeNulo() {
        voluntario.setNome(null);

        InvalidVoluntarioDataException e = assertThrows(InvalidVoluntarioDataException.class, () -> {
            voluntarioService.save(voluntario);
        });

        assertTrue( e.getMessage().contains("O nome do voluntário é obrigatório."));
    }

    @Test
    void save_FalhaEmailJaExiste() {
        when(usuarioRepository.findByEmail("maria@gmail.com")).thenReturn(Optional.of(Usuario.builder().id(5L).email("maria@gmail.com").build()));

        EmailAlreadyExistsException e = assertThrows(EmailAlreadyExistsException.class, () -> {
            voluntarioService.save(voluntario);
        });

        assertTrue(e.getMessage().contains("já está em uso"));
        verify(usuarioRepository).findByEmail("maria@gmail.com");
        verify(usuarioRepository, never()).save(any());
        verify(voluntarioRepository, never()).save(any());
    }

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
        when(escalaRepository.existsByVoluntarioId(anyLong())).thenReturn(false);
        doNothing().when(voluntarioRepository).deleteById(1L);

        assertTrue(voluntarioService.delete(1L));
        verify(voluntarioRepository).deleteById(1L);
    }

    @Test
    void delete_FalhaVoluntarioEmEscala() {
        when(voluntarioRepository.existsById(1L)).thenReturn(true);
        when(escalaRepository.existsByVoluntarioId(anyLong())).thenReturn(true);

        VoluntarioIsScheduledException e = assertThrows(VoluntarioIsScheduledException.class, () -> {
            voluntarioService.delete(1L);
        });

        assertTrue(e.getMessage().contains("Voluntário não pode ser deletado pois está escalado"));  }

    @Test
    void delete_FalhaVoluntarioNaoEncontrado() {
        when(voluntarioRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> {
            voluntarioService.delete(99L);
        });

        assertTrue(e.getMessage().contains("Voluntário com ID 99 não encontrado"));
    }
}
