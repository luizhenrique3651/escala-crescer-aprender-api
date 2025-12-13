package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.VoluntarioIsScheduledException;
import com.crescer_aprender.escala.service.VoluntarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("crescer-aprender/voluntarios")
public class VoluntarioController {

    private final VoluntarioService service;

    @Autowired
    public VoluntarioController(VoluntarioService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @GetMapping
    public ResponseEntity<List<Voluntario>> getAllVoluntarios() {
        log.info("Solicitação: listar todos os voluntários");
        return service.loadAll()
                .map(list -> {
                    log.info("Lista de voluntários retornada, total={}", list.size());
                    return ResponseEntity.ok(list);
                })
                .orElseGet(() -> {
                    log.info("Nenhum voluntário encontrado");
                    return ResponseEntity.noContent().build();
                });
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PostMapping
    public ResponseEntity<Voluntario> save(@RequestBody Voluntario voluntario) {
        log.info("Solicitação: criar voluntário, nome={}", voluntario.getNome());
        try {
            Voluntario saved = service.save(voluntario);
            log.info("Voluntário criado com sucesso, id={}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (EmailAlreadyExistsException e) {
            log.warn("Falha ao criar voluntário - email já existe: {}", voluntario.getUsuario() != null ? voluntario.getUsuario().getEmail() : "N/A");
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voluntario> update(@PathVariable Long id, @RequestBody Voluntario voluntario) {
        log.info("Solicitação: atualizar voluntário id={}", id);
        try {
            Voluntario updated = service.update(id, voluntario);
            log.info("Voluntário atualizado com sucesso id={}", updated.getId());
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            log.warn("Falha ao atualizar voluntário id={} - não encontrado", id);
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Voluntario> delete(@PathVariable Long id) {
        log.info("Solicitação: deletar voluntário id={}", id);
        try {
            service.delete(id);
            log.info("Voluntário deletado com sucesso id={}", id);
            return ResponseEntity.noContent().build();
        } catch (VoluntarioIsScheduledException e) {
            log.warn("Falha ao deletar voluntário id={} - está escalado", id);
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
