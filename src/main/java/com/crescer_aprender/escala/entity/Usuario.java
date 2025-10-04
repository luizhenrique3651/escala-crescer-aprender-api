package com.crescer_aprender.escala.entity;

import com.crescer_aprender.escala.enums.PerfisUsuariosEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String email;

    @Column(nullable = false)
    String senha;

    @Column(nullable = false)
    PerfisUsuariosEnum role;
}
