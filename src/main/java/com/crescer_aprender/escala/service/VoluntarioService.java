package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return repository.save(voluntario);
    }

    public Optional<List<Voluntario>> loadAll() {
        return Optional.of(repository.findAll());
    }

    public Optional<Voluntario> update(Long id, Voluntario voluntario) {
        return repository.findById(id).map(v -> {
            v.setNome(voluntario.getNome());
            v.setEmail(voluntario.getEmail());
            v.setSenha(voluntario.getSenha());
            v.setDatasDisponiveis(voluntario.getDatasDisponiveis());
            return repository.save(v);
        });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
