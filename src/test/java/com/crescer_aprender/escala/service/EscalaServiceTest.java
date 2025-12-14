package com.crescer_aprender.escala.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.EscalaDia;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.InvalidVoluntarioDataException;
import com.crescer_aprender.escala.exception.VoluntarioNotExistException;
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

    private Voluntario criaVoluntarioExemplo(Long id, LocalDate... datas) {
        Voluntario.VoluntarioBuilder builder = Voluntario.builder()
                .id(id)
                .nome("Voluntario " + id);
        if (datas != null && datas.length > 0) {
            builder.datasDisponiveis(new ArrayList<>(Arrays.asList(datas)));
        } else {
            builder.datasDisponiveis(new ArrayList<>());
        }
        return builder.build();
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

        // criar 4 voluntários para cada data (mínimo exigido)
        List<Voluntario> voluntariosData1 = Arrays.asList(
                criaVoluntarioExemplo(1L, LocalDate.of(2025,7,5)),
                criaVoluntarioExemplo(2L, LocalDate.of(2025,7,5)),
                criaVoluntarioExemplo(3L, LocalDate.of(2025,7,5)),
                criaVoluntarioExemplo(4L, LocalDate.of(2025,7,5))
        );
        List<Voluntario> voluntariosData2 = Arrays.asList(
                criaVoluntarioExemplo(5L, LocalDate.of(2025,7,12)),
                criaVoluntarioExemplo(6L, LocalDate.of(2025,7,12)),
                criaVoluntarioExemplo(7L, LocalDate.of(2025,7,12)),
                criaVoluntarioExemplo(8L, LocalDate.of(2025,7,12))
        );

        List<Voluntario> todos = new ArrayList<>();
        todos.addAll(voluntariosData1);
        todos.addAll(voluntariosData2);

        when(escalaRepository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes())).thenReturn(Optional.empty());
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 5)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosData1)));
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 12)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosData2)));
        when(escalaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(voluntarioRepository.findVoluntariosByIds(Mockito.anyList())).thenReturn(Optional.of(todos));

        Escala saved = escalaService.save(escala);
        assertNotNull(saved);
        assertFalse(saved.getVoluntarios().isEmpty());
        // Verifica se foram criados dias com voluntários
        assertNotNull(saved.getDias());
        assertEquals(2, saved.getDias().size());
        for (EscalaDia dia : saved.getDias()) {
            assertTrue(dia.getVoluntarios().size() >= 4 && dia.getVoluntarios().size() <= 8);
        }
        // Verifica se total de voluntários na lista agregada não ultrapassa 8 por dia (mas como union pode ser 8)
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
        // para o update, mocks de disponibilidade por data (mínimo 4 voluntários)
        List<Voluntario> disponiveisData1 = Arrays.asList(criaVoluntarioExemplo(1L, LocalDate.of(2025,7,5)), criaVoluntarioExemplo(2L, LocalDate.of(2025,7,5)), criaVoluntarioExemplo(3L, LocalDate.of(2025,7,5)), criaVoluntarioExemplo(4L, LocalDate.of(2025,7,5)));
        List<Voluntario> disponiveisData2 = Arrays.asList(criaVoluntarioExemplo(5L, LocalDate.of(2025,7,12)), criaVoluntarioExemplo(6L, LocalDate.of(2025,7,12)), criaVoluntarioExemplo(7L, LocalDate.of(2025,7,12)), criaVoluntarioExemplo(8L, LocalDate.of(2025,7,12)));
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025,7,5))).thenReturn(Optional.of(disponiveisData1));
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025,7,12))).thenReturn(Optional.of(disponiveisData2));
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

    @Test
    void testSave_VoluntariosAusentes_DeveLancarException() {
        Escala escala = criaEscalaExemplo();
        escala.setVoluntarios(new ArrayList<>());

        List<Voluntario> voluntariosData1 = Arrays.asList(criaVoluntarioExemplo(1L));
        List<Voluntario> voluntariosData2 = Arrays.asList(criaVoluntarioExemplo(2L));

        when(escalaRepository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes())).thenReturn(Optional.empty());
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 5)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosData1)));
        when(voluntarioRepository.findVoluntariosByData(LocalDate.of(2025, 7, 12)))
                .thenReturn(Optional.of(new ArrayList<>(voluntariosData2)));
        // Simula que só o voluntário 1 existe no banco (2 está ausente)
        when(voluntarioRepository.findVoluntariosByIds(List.of(1L, 2L))).thenReturn(Optional.of(new ArrayList<>(List.of(criaVoluntarioExemplo(1L)))));

        InvalidVoluntarioDataException exception = assertThrows(InvalidVoluntarioDataException.class,
                () -> escalaService.save(escala));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("2"));
        verify(escalaRepository, never()).save(any());
    }

//    @Test
//    void testSave_SemDatasNaoAdicionaVoluntarios() {
//        Escala escala = criaEscalaExemplo();
//        escala.setDatas(Collections.emptyList());
//        escala.setVoluntarios(new ArrayList<>());
//
//        when(escalaRepository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes())).thenReturn(Optional.empty());
//        when(escalaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//        Escala saved = escalaService.save(escala);
//        assertNotNull(saved);
//        assertTrue(saved.getVoluntarios().isEmpty());
//        verify(escalaRepository, times(1)).save(any());
//    }
}
