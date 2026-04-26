package com.crescer_aprender.escala.dto;

import com.crescer_aprender.escala.validation.IsSaturday;
import java.time.LocalDate;
import java.util.List;

public record ScheduleGenerationRequest(
    @IsSaturday
    List<LocalDate> dates
) {}
