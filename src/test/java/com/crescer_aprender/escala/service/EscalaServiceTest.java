package com.crescer_aprender.escala.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.VoluntarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

class EscalaServiceTest {

    @Mock
    private EscalaRepository escalaRepository;

    @Mock
    private VoluntarioRepository voluntarioRepository;

    @InjectMocks
    private EscalaService escalaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Escala criaEscalaExemplo() {
        return Escala.builder()
                .id(1L)
                .ano(2025L)
                .mes(7)
                .datas(Arrays.asList(LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 12)))
                .voluntarios(new ArrayList<>())
                .build();
    }

    private Voluntario criaVoluntarioExemplo(Long id) {
        return Voluntario.builder()
                .id(id)
                .nome("Voluntario " + id)
                .datasDisponiveis(Arrays.asList(LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 12)))
                .build();
    }

    @Test
    void testFindEscalaByMesAnoVoluntario_Sucesso() {
        Escala escala = criaEscalaExemplo();
        when(escalaRepository.findEscalaByMesAnoVoluntario(escala.getMes(), escala.getAno(), 1L))
                .thenReturn(Optional.of(escala));

        Optional<Escala> resultado = escalaService.findEscalaByMesAnoVoluntario(escala.getMes(), escala.getAno(), 1L);
        assertTrue(resultado.isPresent());
        assertEquals(escala, resultado.get());
    }

    @Test
    void testFindEscalaByMesAnoVoluntario_NaoEncontrado() {
        when(escalaRepository.findEscalaByMesAnoVoluntario(anyInt(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> escalaService.findEscalaByMesAnoVoluntario(7, 2025L, 1L));

        assertTrue(exception.getMessage().contains("Escala não encontrada"));
    }

    @Test
    void testFindById_Presente() {
        Escala escala = criaEscalaExemplo();
        when(escalaRepository.findById(1L)).thenReturn(Optional.of(escala));

        Optional<Escala> resultado = escalaService.findById(1L);
        assertTrue(resultado.isPresent());
        assertEquals(escala, resultado.get());
    }

    @Test
    void testFindById_NaoEncontrado() {
        when(escalaRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Escala> resultado = escalaService.findById(1L);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testLoadAll_ComResultados() {
        List<Escala> escalas = Arrays.asList(criaEscalaExemplo());
        when(escalaRepository.findAll()).thenReturn(escalas);

        Optional<List<Escala>> resultado = escalaService.loadAll();
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().size());
    }

    @Test
    void testLoadAll_Vazio() {
        when(escalaRepository.findAll()).thenReturn(Collections.emptyList());

        Optional<List<Escala>> resultado = escalaService.loadAll();
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isEmpty());
    }

    @Test
    void testSave_ComEscalaExistente_DeveLancarExcecao() {
        Escala escala = criaEscalaExemplo();

        when(escalaRepository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes())).thenReturn(Optional.of(escala));

        EscalaAlreadyExistsException exception = assertThrows(EscalaAlreadyExistsException.class,
                () -> escalaService.save(escala));

        assertTrue(exception.getMessage().contains("já existe"));
        verify(escalaRepository, never()).save(any());
    }

    @Test
    void testSave_SemVoluntariosAdicionaDisponiveis() {
        Escala escala = criaEscalaExemplo();
        escala.setVoluntarios(new ArrayList<>());

        List<Voluntario> voluntariosDisponiveisData1 = Arrays.asList(criaVoluntarioExemplo(1L), criaVoluntarioExemplo(2L));
        List<Voluntario> voluntariosDisponiveisData2 = Arrays.asList(criaVoluntarioExemplo(3L), criaVoluntarioExemplo(4L));
        List<Voluntario> todosVoluntariosDisponiveisNasDuasDatas = new ArrayList<>();
        todosVoluntariosDisponiveisNasDuasDatas.addAll(voluntariosDisponiveisData1);
        todosVoluntariosDisponiveisNasDuasDatas.addAll(voluntariosDisponiveisData2);

        when(escalaRepository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes())).thenReturn(Optional.empty());
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 5)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosDisponiveisData1)));
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 12)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosDisponiveisData2)));
        when(escalaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(voluntarioRepository.findVoluntariosByIds(List.of(1L, 2L, 3L, 4L))).thenReturn(Optional.of(todosVoluntariosDisponiveisNasDuasDatas));

        Escala saved = escalaService.save(escala);
        assertNotNull(saved);
        assertFalse(saved.getVoluntarios().isEmpty());
        // Verifica se total de voluntários não ultrapassa 8 conforme lógica
        assertTrue(saved.getVoluntarios().size() <= 8);
    }

    @Test
    void testUpdate_Sucesso() {
        Escala antiga = criaEscalaExemplo();
        Escala atualizacao = Escala.builder()
                .ano(2026L)
                .mes(8)
                .datas(new ArrayList<>(Arrays.asList(LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 12))))
                .voluntarios(Arrays.asList(criaVoluntarioExemplo(99L)))
                .build();

        when(escalaRepository.findById(1L)).thenReturn(Optional.of(antiga));
        when(escalaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Escala atualizada = escalaService.update(1L, atualizacao);

        assertEquals(2026L, atualizada.getAno());
        assertEquals(8, atualizada.getMes());
        assertTrue(atualizada.getDatas().contains(LocalDate.of(2025, 7, 5)));
        assertTrue(atualizada.getVoluntarios().stream().anyMatch(v -> v.getId().equals(99L)));
    }

    @Test
    void testUpdate_EscalaNaoEncontrada() {
        when(escalaRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> escalaService.update(1L, criaEscalaExemplo()));

        assertTrue(exception.getMessage().contains("não encontrado"));
    }

    @Test
    void testDelete_Sucesso() {
        when(escalaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(escalaRepository).deleteById(1L);

        assertTrue(escalaService.delete(1L));
        verify(escalaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_EscalaNaoEncontrada() {
        when(escalaRepository.existsById(1L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> escalaService.delete(1L));

        assertTrue(exception.getMessage().contains("não encontrado"));
        verify(escalaRepository, never()).deleteById(anyLong());
    }

    @Test
    void testFindByFilters_NonPaged_ReturnsList() {
        Escala escala = criaEscalaExemplo();
        Map<String, String> filters = Map.of("mes", String.valueOf(escala.getMes()));
        List<Escala> expected = List.of(escala);

        when(escalaRepository.findAll(Mockito.<Specification<Escala>>any())).thenReturn(expected);

        Optional<List<Escala>> resultado = escalaService.findByFiltersWithoutPagination(filters);
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().size());
        assertEquals(escala, resultado.get().get(0));
    }

    @Test
    void testFindByFilters_Paginated_ReturnsPage() {
        Escala escala = criaEscalaExemplo();
        Map<String, String> filters = Map.of("mes", String.valueOf(escala.getMes()));
        List<Escala> content = List.of(escala);
        Page<Escala> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);

        when(escalaRepository.findAll(Mockito.<Specification<Escala>>any(), any(Pageable.class)))
                .thenReturn(page);

        Page<Escala> resultado = escalaService.findByFiltersPaginated(filters, PageRequest.of(0, 10));
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals(escala, resultado.getContent().get(0));
    }
}
