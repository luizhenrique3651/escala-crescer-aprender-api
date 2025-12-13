package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.service.UsuarioService;
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
@RequestMapping("crescer-aprender/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final VoluntarioService voluntarioService;

    @Autowired
    public UsuarioController(UsuarioService service, VoluntarioService voluntarioService) {
        this.service = service;
        this.voluntarioService = voluntarioService;
    }
    @PreAuthorize("hasAuthority('COORDENADOR')")
    @PostMapping
    public ResponseEntity<Voluntario> save(@RequestBody Voluntario voluntario) {
        log.info("Solicitação: criar usuário + voluntário, nomeVoluntario={}", voluntario.getNome());
        try {
            Voluntario saved = voluntarioService.save(voluntario);
            log.info("Voluntário (com usuário) criado com sucesso id={}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (EmailAlreadyExistsException e) {
            log.warn("Falha ao criar usuário/voluntário - email já existe: {}", voluntario.getUsuario() != null ? voluntario.getUsuario().getEmail() : "N/A");
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @GetMapping
    public ResponseEntity<List<Usuario>> getAll() {
        log.info("Solicitação: listar todos os usuários");
        List<Usuario> usuarios = service.loadAll();
        if (usuarios == null || usuarios.isEmpty()) {
            log.info("Nenhum usuário encontrado");
            return ResponseEntity.noContent().build();
        }
        log.info("Lista de usuários retornada, total={}", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }
}
