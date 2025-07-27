package com.crescer_aprender.escala.repository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long>{
    @Query("SELECT esc FROM Escala esc JOIN esc.voluntarios v WHERE esc.mes = :mes AND esc.ano = :ano AND v.id = :voluntario")
    Optional<Escala> findEscalaByMesAnoVoluntario(@Param("mes") Integer mes, @Param("ano") Long ano, @Param("voluntario") Long voluntario);

    Optional<Escala> findByAnoAndMes(Long ano, Integer mes);

    Boolean existsByVoluntarios(Voluntario voluntario);

    @Query("SELECT esc FROM Escala esc JOIN esc.voluntarios v WHERE v = :voluntario")
    Optional<List<Escala>> findByVoluntario(@Param("voluntario") Voluntario voluntario);

}

