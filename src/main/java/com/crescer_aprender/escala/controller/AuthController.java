package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.UserEmailNotFoundException;
import com.crescer_aprender.escala.security.JwtService;
import com.crescer_aprender.escala.service.VoluntarioService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private VoluntarioService voluntarioService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        log.info("Tentativa de login para usuário={}", authRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getSenha())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Voluntario userData = voluntarioService.findByUsuarioEmail(authRequest.email).orElseThrow(() -> new UserEmailNotFoundException(authRequest.getEmail()));
            String token = jwtService.generateToken(userDetails);
            log.info("Login bem-sucedido para usuário={}", authRequest.getEmail());
            return ResponseEntity.ok(new AuthResponse(userData, token));
        } catch (Exception e) {
            log.warn("Falha no login para usuário={}: {}", authRequest.getEmail(), e.getMessage());
            // Não expor mensagem de exceção interna ao cliente
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Data
    public static class AuthRequest {
        private String email;
        private String senha;
    }
    @Data
    public static class AuthResponse {
        private Long voluntarioId;
        private String email;
        private String nome;
        private String role;
        private List<LocalDate> datasDisponiveis;
        private String token;

        public AuthResponse(Voluntario user, String token) {
            this.voluntarioId = user.getId();
            this.email = user.getUsuario().getEmail();
            this.nome = user.getNome();
            this.role = user.getUsuario().getRole().name();
            this.datasDisponiveis = user.getDatasDisponiveis();
            this.token = token;
        }
    }
}
