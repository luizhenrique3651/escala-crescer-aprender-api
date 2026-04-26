package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.dto.ScheduleGenerationRequest;
import com.crescer_aprender.escala.dto.ScheduleGenerationResponse;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduleGenerationServiceTest {

    @Mock
    private VoluntarioRepository voluntarioRepository;

    @Mock
    private CapacityGuard capacityGuard;

    @InjectMocks
    private ScheduleGenerationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerate_Success() {
        LocalDate date = LocalDate.of(2025, 5, 10); // Sábado
        ScheduleGenerationRequest request = new ScheduleGenerationRequest(List.of(date));

        List<Voluntario> available = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            available.add(Voluntario.builder().id(i).nome("V" + i).build());
        }

        when(voluntarioRepository.findVoluntariosByData(date)).thenReturn(Optional.of(available));
        doNothing().when(capacityGuard).validate(anyInt());

        ScheduleGenerationResponse response = service.generate(request);

        assertNotNull(response);
        assertEquals(1, response.days().size());
        assertEquals(5, response.days().getFirst().volunteers().size());
        verify(capacityGuard, times(1)).validate(5);
    }

    @Test
    void testGenerate_BalancePriority() {
        LocalDate d1 = LocalDate.of(2025, 5, 10);
        LocalDate d2 = LocalDate.of(2025, 5, 17);
        ScheduleGenerationRequest request = new ScheduleGenerationRequest(List.of(d1, d2));

        Voluntario v1 = Voluntario.builder().id(1L).nome("V1").build();
        Voluntario v2 = Voluntario.builder().id(2L).nome("V2").build();
        
        // No dia 1, ambos disponíveis
        when(voluntarioRepository.findVoluntariosByData(d1)).thenReturn(Optional.of(List.of(v1, v2)));
        // No dia 2, ambos disponíveis
        when(voluntarioRepository.findVoluntariosByData(d2)).thenReturn(Optional.of(List.of(v1, v2)));
        
        // Simula que queremos apenas 1 por dia para testar o balanceamento (limit 8 na classe real, mas v1 e v2 estarão lá)
        // A lógica real limit(8) selecionará ambos se houver apenas 2.
        
        ScheduleGenerationResponse response = service.generate(request);
        
        assertNotNull(response);
        assertEquals(2, response.days().size());
        // v1 e v2 devem aparecer nos summaries de ambos os dias pois o limit é 8
        assertEquals(2, response.days().get(0).volunteers().size());
        assertEquals(2, response.days().get(1).volunteers().size());
    }
}
