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
        try {
            Voluntario saved = voluntarioService.save(voluntario);
            return ResponseEntity.ok(saved);
        } catch (EmailAlreadyExistsException e) {
            return new ResponseEntity<>(Voluntario.builder().errorMessage(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAuthority('COORDENADOR')")
    @GetMapping
    public ResponseEntity<List<Usuario>> getAll() {
        List<Usuario> usuarios = service.loadAll();
        if (usuarios == null || usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }
}
