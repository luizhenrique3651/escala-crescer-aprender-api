package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
public class EscalaService {

    private final EscalaRepository repository;

    @Autowired
    public EscalaService(EscalaRepository repository) {
        this.repository = repository;
    }

    public Optional<Escala> findEscalaByMesAnoVoluntario(Integer mes, Long ano, Voluntario voluntario) {
        return repository.findEscalaByMesAnoVoluntario(mes, ano, voluntario);
    }

    public Optional<Escala> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<List<Escala>> loadAll() {
        return Optional.of(repository.findAll());
    }

    public Escala save(Escala escala) {
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno(), escala.getMes());
        if (escalaExistente.isPresent()) {
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }
        return repository.save(escala);
    }

    public Escala update(Long id, Escala escala) {
        Escala oldEscala = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas())
                .ifPresent(dates -> mergeDatas(oldEscala, dates));
        Optional.ofNullable(escala.getVoluntarios()).ifPresent(voluntarios -> mergeVoluntarios(oldEscala, voluntarios));
        return repository.save(oldEscala);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            throw new EntityNotFoundException("Escala", id);
        }
    }

    private void mergeVoluntarios(Escala escala, List<Voluntario> novosVoluntarios) {
        List<Voluntario> voluntariosAtuais = escala.getVoluntarios();

        voluntariosAtuais.removeIf(voluntario -> !novosVoluntarios.contains(voluntario));
        novosVoluntarios.stream().filter(voluntario -> !voluntariosAtuais.contains(voluntario)).forEach(voluntariosAtuais::add);

    }

    private void mergeDatas(Escala escala, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = escala.getDatas();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream()
                .filter(date -> !datasAtuais.contains(date))
                .forEach(datasAtuais::add);
    }


}
