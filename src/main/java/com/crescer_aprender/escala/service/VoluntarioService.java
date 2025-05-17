package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.ConstantExceptionUtil;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.InvalidVoluntarioDataException;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.hibernate.action.internal.EntityActionVetoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VoluntarioService {

    private final VoluntarioRepository repository;

    @Autowired
    public VoluntarioService(VoluntarioRepository repository) {
        this.repository = repository;
    }

    public Voluntario save(Voluntario voluntario) {
        if (voluntario.getNome() == null || voluntario.getNome().isEmpty()) {
            throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_NAME);
        }
        if (voluntario.getEmail() == null || !voluntario.getEmail().contains("@")) {
            throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_EMAIL);
        }
        return repository.save(voluntario);
    }


    public Optional<List<Voluntario>> loadAll() {
        return Optional.of(repository.findAll());
    }

    public Voluntario update(Long id, Voluntario voluntario) {
        Voluntario oldVoluntario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voluntário", id));

        Optional.ofNullable(voluntario.getNome()).ifPresent(oldVoluntario::setNome);
        Optional.ofNullable(voluntario.getEmail()).ifPresent(oldVoluntario::setEmail);
        Optional.ofNullable(voluntario.getSenha()).ifPresent(oldVoluntario::setSenha);
        Optional.ofNullable(voluntario.getDatasDisponiveis())
                .ifPresent(dates -> mergeDatasDisponiveis(oldVoluntario, dates));

        return repository.save(oldVoluntario);
    }

    private void mergeDatasDisponiveis(Voluntario voluntario, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = voluntario.getDatasDisponiveis();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream()
                .filter(date -> !datasAtuais.contains(date))
                .forEach(datasAtuais::add);
    }


    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
