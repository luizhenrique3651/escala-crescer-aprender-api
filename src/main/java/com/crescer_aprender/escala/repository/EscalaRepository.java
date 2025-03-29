package com.crescer_aprender.escala.repository;

import java.time.Month;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long>{

	@Query("SELECT Escala esc FROM Escala WHERE esc.mes = ? AND esc.ano = ? AND esc.voluntarios.contains(voluntario)")
	Optional<Escala> findEscalaByMesAnoVoluntario(Month mes, Long ano, Voluntario voluntario);
}
