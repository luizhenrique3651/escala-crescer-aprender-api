package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.service.EscalaService;
import org.springframework.beans.factory.annotation.Autowired;
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
        Escala saved = escalaService.save(escala);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escala> update(@PathVariable Long id, @RequestBody Escala escala) {
        Escala updated = escalaService.update(id, escala);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        escalaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/buscar-por-mes-ano-voluntario")
    public ResponseEntity<Escala> findByMesAnoVoluntario(
            @RequestParam Integer mes,
            @RequestParam Long ano,
            @RequestBody Voluntario voluntario) {

        return escalaService.findEscalaByMesAnoVoluntario(mes, ano, voluntario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
