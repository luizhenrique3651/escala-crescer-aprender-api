package com.crescer_aprender.escala.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscalaDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "DATA_ESCALA_DIA")
    LocalDate data;

    @ManyToMany
    @JoinTable(
            name = "escala_dia_voluntario",
            joinColumns = @JoinColumn(name = "escala_dia_id"),
            inverseJoinColumns = @JoinColumn(name = "voluntario_id")
    )
    List<Voluntario> voluntarios;

    @ManyToOne
    @JoinColumn(name = "escala_id")
    Escala escala;

    @Transient
    String errorMessage;
}

