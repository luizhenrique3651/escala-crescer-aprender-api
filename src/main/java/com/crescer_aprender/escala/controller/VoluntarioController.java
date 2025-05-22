package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.service.VoluntarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Voluntario>> getAllVoluntarios() {
        return service.loadAll()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<Voluntario> save(@RequestBody Voluntario voluntario) {
        Voluntario saved = service.save(voluntario);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voluntario> update(@PathVariable Long id, @RequestBody Voluntario voluntario) {
        Voluntario updated = service.update(id, voluntario);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
