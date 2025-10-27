package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Usuario;
import com.crescer_aprender.escala.enums.PerfisUsuariosEnum;
import com.crescer_aprender.escala.exception.EmailAlreadyExistsException;
import com.crescer_aprender.escala.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (usuario.getEmail() != null && repository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(usuario.getEmail());
        }
        if (usuario.getRole() == null) {
            usuario.setRole(PerfisUsuariosEnum.VOLUNTARIO);
        }
        if (usuario.getSenha() != null) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        return repository.save(usuario);
    }

    public List<Usuario> loadAll() {
        return repository.findAll();
    }
}

