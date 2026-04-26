package com.crescer_aprender.escala.dto;

import java.time.LocalDate;
import java.util.List;

public record ScheduleGenerationResponse(
    List<GeneratedDay> days
) {
    public record GeneratedDay(
        LocalDate date,
        List<VolunteerSummary> volunteers
    ) {}

    public record VolunteerSummary(
        Long id,
        String name
    ) {}
}
