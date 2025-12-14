package com.crescer_aprender.escala.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscalaCreateRequest {
    Integer mes;
    Long ano;
    List<LocalDate> datas;
    // opcional: lista de dias com IDs de voluntarios por dia
    List<EscalaDiaRequest> dias;
    // opcional: lista de ids de voluntarios (legacy)
    List<Long> voluntarios;
}

