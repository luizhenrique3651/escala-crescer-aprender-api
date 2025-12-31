package com.crescer_aprender.escala.dto;

import com.crescer_aprender.escala.entity.EscalaDia;
import com.crescer_aprender.escala.entity.Voluntario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscalaDiaRequest {
    LocalDate data;
    // Lista de IDs de voluntários para essa data (preferível). Se null, o service irá selecionar automaticamente baseado em disponibilidade.
    List<Long> voluntarios;

    public static EscalaDiaRequest of(EscalaDia escalaDia){
        return EscalaDiaRequest.builder()
                .data(escalaDia.getData())
                .voluntarios(escalaDia.getVoluntarios().stream().map(Voluntario::getId).toList())
                .build();
    }
    public static List<EscalaDiaRequest> of(List<EscalaDia> escalaDias){
         List<EscalaDiaRequest> lista = new ArrayList<>();
        for(EscalaDia dia: escalaDias){
            lista.add(EscalaDiaRequest.of(dia));
        }
        return lista;
    }
}

