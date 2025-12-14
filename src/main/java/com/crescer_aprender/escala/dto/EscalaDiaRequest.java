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
public class EscalaDiaRequest {
    LocalDate data;
    // Lista de IDs de voluntários para essa data (preferível). Se null, o service irá selecionar automaticamente baseado em disponibilidade.
    List<Long> voluntarios;
}

