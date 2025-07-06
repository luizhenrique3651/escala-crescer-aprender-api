package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.service.EscalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("crescer-aprender/escala")
public class EscalaController {

    private final EscalaService escalaService;

    @Autowired
    public EscalaController(EscalaService escalaService) {
        this.escalaService = escalaService;
    }

    @GetMapping
    public ResponseEntity<List<Escala>> getAll() {
        Optional<List<Escala>> escalas = escalaService.loadAll();
        return escalas.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Escala> getById(@PathVariable Long id) {
        return escalaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Escala> create(@RequestBody Escala escala) {
        try {
            Escala saved = escalaService.save(escala);
            return ResponseEntity.ok(saved);
        } catch (EscalaAlreadyExistsException e) {
            return new ResponseEntity(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escala> update(@PathVariable Long id, @RequestBody Escala escala) {
        try {
            Escala updated = escalaService.update(id, escala);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            escalaService.delete(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/buscar-por-mes-ano-voluntario")
    public ResponseEntity<Escala> findByMesAnoVoluntario(
            @RequestParam Integer mes,
            @RequestParam Long ano,
            @RequestBody Voluntario voluntario) {
        try {
            return ResponseEntity.ok(escalaService.findEscalaByMesAnoVoluntario(mes, ano, voluntario).get());
        }catch (EntityNotFoundException e) {
            return new ResponseEntity(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }
}