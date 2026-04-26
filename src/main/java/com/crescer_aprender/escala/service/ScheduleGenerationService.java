package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.dto.ScheduleGenerationRequest;
import com.crescer_aprender.escala.dto.ScheduleGenerationResponse;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleGenerationService {

    private final VoluntarioRepository voluntarioRepository;
    private final CapacityGuard capacityGuard;

    public ScheduleGenerationResponse generate(ScheduleGenerationRequest request) {
        log.info("Iniciando geração de escala balanceada para {} datas", request.dates().size());
        
        Map<Long, Integer> frequencyMap = new HashMap<>();

        List<ScheduleGenerationResponse.GeneratedDay> generatedDays = request.dates().stream()
            .map(date -> {
                List<Voluntario> available = voluntarioRepository.findVoluntariosByData(date)
                    .orElse(Collections.emptyList());

                List<Voluntario> selected = available.stream()
                    .sorted(Comparator.comparingInt((Voluntario v) -> frequencyMap.getOrDefault(v.getId(), 0))
                        .thenComparing(v -> Math.random()))
                    .limit(8)
                    .toList();

                capacityGuard.validate(selected.size());

                selected.forEach(v -> frequencyMap.put(v.getId(), frequencyMap.getOrDefault(v.getId(), 0) + 1));

                List<ScheduleGenerationResponse.VolunteerSummary> summaries = selected.stream()
                    .map(v -> new ScheduleGenerationResponse.VolunteerSummary(v.getId(), v.getNome()))
                    .toList();

                return new ScheduleGenerationResponse.GeneratedDay(date, summaries);
            })
            .collect(Collectors.toList());

        return new ScheduleGenerationResponse(generatedDays);
    }
}
