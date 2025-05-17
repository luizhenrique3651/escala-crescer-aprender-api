package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EscalaService {

    private final EscalaRepository repository;
    @Autowired
    public EscalaService(EscalaRepository repository) {
        this.repository = repository;
    }

    public Optional<List<Escala>> loadAll(){
        return Optional.of(repository.findAll());
    }

    public Escala save(Escala escala){
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno(), escala.getMes());
        if(escalaExistente.isPresent()){
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }
            return repository.save(escala);
    }

    public Escala update(Long id, Escala escala){
        Escala oldEscala = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas())
                .ifPresent(dates -> mergeDatas(oldEscala, dates));


    }

    private void mergeDatas(Escala escala, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = escala.getDatas();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream()
                .filter(date -> !datasAtuais.contains(date))
                .forEach(datasAtuais::add);
    }


}
