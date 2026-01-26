package com.crescer_aprender.escala.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crescer_aprender.escala.entity.Voluntario;

@Repository
public interface VoluntarioRepository extends JpaRepository<Voluntario, Long>{

    @Query("SELECT v FROM Voluntario v JOIN v.datasDisponiveis d WHERE d = :data")
    Optional<List<Voluntario>> findVoluntariosByData(@Param("data") LocalDate data);

    @Query("SELECT v FROM Voluntario v WHERE v.id IN :ids")
    Optional<List<Voluntario>> findVoluntariosByIds(@Param("ids") List<Long> ids);

    Optional<Voluntario> findByUsuarioEmail(String email);

    @Query("SELECT v.datasDisponiveis FROM Voluntario v WHERE v.id = :idVoluntario")
    List<LocalDate> getDatasDisponiveisByIdVoluntario(Long idVoluntario);

}
