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
        return service.loadAll()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PostMapping
    public ResponseEntity<Voluntario> save(@RequestBody Voluntario voluntario) {
        try {
            Voluntario saved = service.save(voluntario);
            return ResponseEntity.ok(saved);
        } catch (EmailAlreadyExistsException e) {
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voluntario> update(@PathVariable Long id, @RequestBody Voluntario voluntario) {
        try {
            Voluntario updated = service.update(id, voluntario);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Voluntario> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (VoluntarioIsScheduledException e) {
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
