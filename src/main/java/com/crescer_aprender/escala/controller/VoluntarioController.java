package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.exception.VoluntarioIsScheduledException;
import com.crescer_aprender.escala.service.VoluntarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> save(@RequestBody Voluntario voluntario) {
        try{
            Voluntario saved = service.save(voluntario);
            return ResponseEntity.ok(saved.toString());

        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voluntario> update(@PathVariable Long id, @RequestBody Voluntario voluntario) {
        Voluntario updated = service.update(id, voluntario);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
      try {
          service.delete(id);
          return ResponseEntity.noContent().build();
      } catch (VoluntarioIsScheduledException e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
      }
    }
}
