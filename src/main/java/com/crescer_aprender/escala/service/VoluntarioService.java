package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.enums.PerfisUsuariosEnum;
import com.crescer_aprender.escala.exception.*;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.UsuarioRepository;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VoluntarioService {

    private final VoluntarioRepository repository;
    private final EscalaRepository escalaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public VoluntarioService(VoluntarioRepository repository, EscalaRepository escalaRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.escalaRepository = escalaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Voluntario save(Voluntario voluntario) {
        if (voluntario.getNome() == null || voluntario.getNome().isEmpty()) {
            throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_NAME);
        }

        // Se houver um Usuário aninhado, valide e persista primeiro
        if (voluntario.getUsuario() != null) {
            if (voluntario.getUsuario().getEmail() == null || !voluntario.getUsuario().getEmail().contains("@")) {
                throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_EMAIL);
            }
            if (usuarioRepository.findByEmail(voluntario.getUsuario().getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException(voluntario.getUsuario().getEmail());
            }
            if (voluntario.getUsuario().getRole() == null) {
                voluntario.getUsuario().setRole(PerfisUsuariosEnum.VOLUNTARIO);
            }
            if (voluntario.getUsuario().getSenha() != null) {
                voluntario.getUsuario().setSenha(passwordEncoder.encode(voluntario.getUsuario().getSenha()));
            }
            // salve o usuário primeiro para garantir que ele tenha um id (nenhuma cascata configurada)
            try {
                usuarioRepository.save(voluntario.getUsuario());
            } catch (DataIntegrityViolationException dive) {
                // Em caso de condição de corrida ou violação de restrição de banco de dados, traduzir para exceção de domínio
                throw new EmailAlreadyExistsException(voluntario.getUsuario().getEmail());
            }
        }

        return repository.save(voluntario);
    }


    public Optional<List<Voluntario>> loadAll() {
        return Optional.of(repository.findAll());
    }

    public Voluntario update(Long id, Voluntario voluntario) {
        Voluntario oldVoluntario = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Voluntário", id));

        Optional.ofNullable(voluntario.getNome()).ifPresent(oldVoluntario::setNome);
        Optional.ofNullable(voluntario.getDatasDisponiveis()).ifPresent(dates -> mergeDatasDisponiveis(oldVoluntario, dates));

        Optional<List<Escala>> escalasDoVoluntario = escalaRepository.findByVoluntario(oldVoluntario);

        if(escalasDoVoluntario.isPresent()){
            for (Escala escala : escalasDoVoluntario.get()) {
                // Remover voluntário da escala caso não esteja disponível para alguma das datas da escala
                escala.getVoluntarios().removeIf(v -> v.getId().equals(oldVoluntario.getId()) &&
                        escala.getDatas().stream().anyMatch(date -> !oldVoluntario.getDatasDisponiveis().contains(date)));
                escalaRepository.save(escala);
            }

        }
        return repository.save(oldVoluntario);
    }

    public Optional<List<Voluntario>> findVoluntariosByData(LocalDate data) {
        return repository.findVoluntariosByData(data);
    }

    private void mergeDatasDisponiveis(Voluntario voluntario, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = voluntario.getDatasDisponiveis();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream().filter(date -> !datasAtuais.contains(date)).forEach(datasAtuais::add);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            if(escalaRepository.existsByVoluntarios(Voluntario.builder().id(id).build())){
                throw new VoluntarioIsScheduledException();
            }
            repository.deleteById(id);
            return true;
        } else {
            throw new EntityNotFoundException("Voluntário", id);
        }
    }
}
