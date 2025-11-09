package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.VoluntarioNotExistException;
import com.crescer_aprender.escala.service.EscalaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
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
        log.info("Recebida requisição para listar todas as escalas.");
        Optional<List<Escala>> escalas = escalaService.loadAll();
        return escalas.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("byId/{id}")
    public ResponseEntity<Escala> getById(@PathVariable Long id) {
        log.info("Recebida requisição para buscar escala por ID: {}", id);
        return escalaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("byDate/{data}")
    public ResponseEntity<Escala> getByAnoAndMes(@PathVariable LocalDate data){
        log.info("Recebida requisição para buscar escala por Ano e Mês: {}", data);
        return escalaService.findByAnoAndMes(data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PostMapping
    public ResponseEntity<Escala> create(@RequestBody Escala escala) {
        log.info("Recebida requisição para criar nova escala.");
        try {
            Escala saved = escalaService.save(escala);
            return ResponseEntity.ok(saved);
        } catch (EscalaAlreadyExistsException e) {
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        } catch(VoluntarioNotExistException e){
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Escala> update(@PathVariable Long id, @RequestBody Escala escala) {
        try {
            Escala updated = escalaService.update(id, escala);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Escala> delete(@PathVariable Long id) {
        try {
            escalaService.delete(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/buscar-por-mes-ano-voluntario")
    public ResponseEntity<Escala> findByMesAnoVoluntario(
            @RequestParam Integer mes,
            @RequestParam Long ano,
            @RequestParam Long idVoluntario) {
        try {
            return ResponseEntity.ok(escalaService.findEscalaByMesAnoVoluntario(mes, ano, idVoluntario).get());
        }catch (EntityNotFoundException e) {
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Busca dinâmica e paginada por query params. Exemplo: /search?mes=5&ano=2025
     * Aceita qualquer par chave=valor; chaves suportadas por EscalaSpecifications: id, mes, ano, data(s), voluntario(voluntarioId).
     */
    @GetMapping("/pesquisa")
    public ResponseEntity<Page<Escala>> searchByQueryParams(@RequestParam Map<String, String> params, @PageableDefault Pageable pageable) {
        Page<Escala> results = escalaService.findByFiltersPaginated(params, pageable);
        if (results == null || results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Busca dinâmica e paginada     por body JSON. Exemplo POST /search with body {"mes":"5","ano":"2025"}
     */
    @PostMapping("/pesquisa")
    public ResponseEntity<Page<Escala>> searchByBody(@RequestBody Map<String, String> filters, @PageableDefault Pageable pageable) {
        Page<Escala> results = escalaService.findByFiltersPaginated(filters, pageable);
        if (results == null || results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }

    // endpoints não paginados
    @GetMapping("/pesquisa-legado")
    public ResponseEntity<List<Escala>> searchByQueryParamsList(@RequestParam Map<String, String> params) {
        Optional<List<Escala>> results = escalaService.findByFiltersWithoutPagination(params);
        return results.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/pesquisa-legado")
    public ResponseEntity<List<Escala>> searchByBodyList(@RequestBody Map<String, String> filters) {
        Optional<List<Escala>> results = escalaService.findByFiltersWithoutPagination(filters);
        return results.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}