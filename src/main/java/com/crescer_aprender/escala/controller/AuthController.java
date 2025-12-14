package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.security.JwtService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest) {
        log.info("Tentativa de login para usuário={}", authRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            log.info("Login bem-sucedido para usuário={}", authRequest.getUsername());
            return token;
        } catch (Exception e) {
            log.warn("Falha no login para usuário={}: {}", authRequest.getUsername(), e.getMessage());
            return e.getMessage();
        }
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
}
}
