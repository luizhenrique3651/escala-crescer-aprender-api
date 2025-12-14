package com.crescer_aprender.escala.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long>, JpaSpecificationExecutor<Escala> {
    // agora pesquisamos via EscalaDia -> voluntarios (escala.dias join d.voluntarios)
    @Query("SELECT esc FROM Escala esc JOIN esc.dias d JOIN d.voluntarios v WHERE esc.mes = :mes AND esc.ano = :ano AND v.id = :voluntario")
    Optional<Escala> findEscalaByMesAnoVoluntario(@Param("mes") Integer mes, @Param("ano") Long ano, @Param("voluntario") Long voluntario);

    Optional<Escala> findByAnoAndMes(Integer ano, Integer mes);

    // verifica se existe alguma escala onde o voluntario está alocado em algum dia
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Escala esc JOIN esc.dias d JOIN d.voluntarios v WHERE v.id = :voluntarioId")
    Boolean existsByVoluntarioId(@Param("voluntarioId") Long voluntarioId);

    @Query("SELECT esc FROM Escala esc JOIN esc.dias d JOIN d.voluntarios v WHERE v = :voluntario")
    Optional<List<Escala>> findByVoluntario(@Param("voluntario") Voluntario voluntario);

}
