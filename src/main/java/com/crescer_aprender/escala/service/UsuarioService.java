package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.enums.PerfisUsuariosEnum;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario save(Usuario usuario) {
        log.info("Iniciando salvamento de usuário email={}", usuario.getEmail());
        if (usuario.getEmail() != null && repository.findByEmail(usuario.getEmail()).isPresent()) {
            log.warn("Falha ao salvar usuário - email já existe: {}", usuario.getEmail());
            throw new EmailAlreadyExistsException(usuario.getEmail());
        }
        if (usuario.getRole() == null) {
            usuario.setRole(PerfisUsuariosEnum.VOLUNTARIO);
        }
        if (usuario.getSenha() != null) {
            log.debug("Codificando senha para usuário email={}", usuario.getEmail());
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        Usuario saved = repository.save(usuario);
        log.info("Usuário salvo com sucesso id={}", saved.getId());
        return saved;
    }

    public List<Usuario> loadAll() {
        log.info("Carregando todos os usuários do banco");
        return repository.findAll();
    }
}
