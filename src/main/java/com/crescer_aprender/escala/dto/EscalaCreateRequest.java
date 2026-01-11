package com.crescer_aprender.escala.dto;

import com.crescer_aprender.escala.entity.Escala;
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
    // o campo legado 'voluntarios' foi removido - favor usar 'dias[].voluntarios' ou omitir para seleção automática
    Boolean incluirVoluntariosAutomaticamente;
    public static EscalaCreateRequest of(Escala escala){
        return EscalaCreateRequest.builder()
                .mes(escala.getMes())
                .ano(escala.getAno())
                .datas(escala.getDatas())
                .dias(EscalaDiaRequest.of(escala.getDias())).build();
    }
}
