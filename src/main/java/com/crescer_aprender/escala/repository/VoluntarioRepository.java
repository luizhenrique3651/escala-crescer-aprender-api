package com.crescer_aprender.escala.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crescer_aprender.escala.entity.Voluntario;

@Repository
public interface VoluntarioRepository extends JpaRepository<Voluntario, Long>{

	Optional<List<Voluntario>> findVoluntarioByData(LocalDate data);
	
	
	
}
