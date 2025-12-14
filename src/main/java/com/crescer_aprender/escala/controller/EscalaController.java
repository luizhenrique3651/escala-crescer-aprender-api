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
        log.info("Solicitação: listar todas as escalas");
        Optional<List<Escala>> escalas = escalaService.loadAll();
        return escalas.map(list -> {
            log.info("Lista de escalas retornada, total={}", list.size());
            return ResponseEntity.ok(list);
        }).orElseGet(() -> {
            log.info("Nenhuma escala encontrada");
            return ResponseEntity.noContent().build();
        });
    }

    @GetMapping("byId/{id}")
    public ResponseEntity<Escala> getById(@PathVariable Long id) {
        log.info("Solicitação: buscar escala por ID={}", id);
        return escalaService.findById(id)
                .map(e -> {
                    log.info("Escala encontrada id={}", id);
                    return ResponseEntity.ok(e);
                })
                .orElseGet(() -> {
                    log.warn("Escala não encontrada id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("byDate/{data}")
    public ResponseEntity<Escala> getByAnoAndMes(@PathVariable LocalDate data){
        log.info("Solicitação: buscar escala por data={}", data);
        return escalaService.findByAnoAndMes(data)
                .map(e -> {
                    log.info("Escala encontrada para data={}", data);
                    return ResponseEntity.ok(e);
                })
                .orElseGet(() -> {
                    log.warn("Escala não encontrada para data={}", data);
                    return ResponseEntity.notFound().build();
                });
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PostMapping
    public ResponseEntity<Escala> create(@RequestBody Escala escala) {
        log.info("Solicitação: criar nova escala ano={} mes={}", escala.getAno(), escala.getMes());
        try {
            Escala saved = escalaService.save(escala);
            log.info("Escala criada com sucesso id={}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (EscalaAlreadyExistsException e) {
            log.warn("Falha ao criar escala - já existe para ano={} mes={}", escala.getAno(), escala.getMes());
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        } catch(VoluntarioNotExistException e){
            log.warn("Falha ao criar escala - voluntários não existem: {}", e.getMessage());
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Escala> update(@PathVariable Long id, @RequestBody Escala escala) {
        log.info("Solicitação: atualizar escala id={}", id);
        try {
            Escala updated = escalaService.update(id, escala);
            log.info("Escala atualizada com sucesso id={}", updated.getId());
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            log.warn("Falha ao atualizar escala id={} - não encontrada", id);
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Escala> delete(@PathVariable Long id) {
        log.info("Solicitação: deletar escala id={}", id);
        try {
            escalaService.delete(id);
            log.info("Escala deletada com sucesso id={}", id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.warn("Falha ao deletar escala id={} - não encontrada", id);
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/buscar-por-mes-ano-voluntario")
    public ResponseEntity<Escala> findByMesAnoVoluntario(
            @RequestParam Integer mes,
            @RequestParam Long ano,
            @RequestParam Long idVoluntario) {
        log.info("Solicitação: buscar escala por mes={} ano={} voluntarioId={}", mes, ano, idVoluntario);
        try {
            Escala found = escalaService.findEscalaByMesAnoVoluntario(mes, ano, idVoluntario)
                    .orElseThrow(() -> new EntityNotFoundException("Escala não encontrada para " + mes + "/" + ano + " e voluntário: " + idVoluntario));
            log.info("Escala encontrada para mes={} ano={} voluntarioId={}", mes, ano, idVoluntario);
            return ResponseEntity.ok(found);
        }catch (EntityNotFoundException e) {
            log.warn("Escala não encontrada para mes={} ano={} voluntarioId={}", mes, ano, idVoluntario);
            return new ResponseEntity<>(Escala.builder().errorMessage(e.getMessage()).build(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Busca dinâmica e paginada por query params. Exemplo: /search?mes=5&ano=2025
     * Aceita qualquer par chave=valor; chaves suportadas por EscalaSpecifications: id, mes, ano, data(s), voluntario(voluntarioId).
     */
    @GetMapping("/pesquisa")
    public ResponseEntity<Page<Escala>> searchByQueryParams(@RequestParam Map<String, String> params, @PageableDefault Pageable pageable) {
        log.info("Solicitação: pesquisa paginada por params={}", params);
        Page<Escala> results = escalaService.findByFiltersPaginated(params, pageable);
        if (results == null || results.isEmpty()) {
            log.info("Pesquisa paginada sem resultados para params={}", params);
            return ResponseEntity.noContent().build();
        }
        log.info("Pesquisa paginada retornou {} resultados para params={}", results.getTotalElements(), params);
        return ResponseEntity.ok(results);
    }

    /**
     * Busca dinâmica e paginada     por body JSON. Exemplo POST /search with body {"mes":"5","ano":"2025"}
     */
    @PostMapping("/pesquisa")
    public ResponseEntity<Page<Escala>> searchByBody(@RequestBody Map<String, String> filters, @PageableDefault Pageable pageable) {
        log.info("Solicitação: pesquisa paginada (body) filters={}", filters);
        Page<Escala> results = escalaService.findByFiltersPaginated(filters, pageable);
        if (results == null || results.isEmpty()) {
            log.info("Pesquisa (body) sem resultados para filters={}", filters);
            return ResponseEntity.noContent().build();
        }
        log.info("Pesquisa (body) retornou {} resultados para filters={}", results.getTotalElements(), filters);
        return ResponseEntity.ok(results);
    }

    // endpoints não paginados
    @GetMapping("/pesquisa-legado")
    public ResponseEntity<List<Escala>> searchByQueryParamsList(@RequestParam Map<String, String> params) {
        log.info("Solicitação: pesquisa (legado) por params={}", params);
        Optional<List<Escala>> results = escalaService.findByFiltersWithoutPagination(params);
        return results.map(list -> {
            log.info("Pesquisa (legado) retornou total={}", list.size());
            return ResponseEntity.ok(list);
        }).orElseGet(() -> {
            log.info("Pesquisa (legado) sem resultados para params={}", params);
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/pesquisa-legado")
    public ResponseEntity<List<Escala>> searchByBodyList(@RequestBody Map<String, String> filters) {
        log.info("Solicitação: pesquisa (legado) body filters={}", filters);
        Optional<List<Escala>> results = escalaService.findByFiltersWithoutPagination(filters);
        return results.map(list -> {
            log.info("Pesquisa (legado) (body) retornou total={}", list.size());
            return ResponseEntity.ok(list);
        }).orElseGet(() -> {
            log.info("Pesquisa (legado) (body) sem resultados para filters={}", filters);
            return ResponseEntity.noContent().build();
        });
    }
}