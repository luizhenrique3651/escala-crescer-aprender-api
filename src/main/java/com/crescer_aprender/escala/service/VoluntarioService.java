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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Iniciando salvamento de voluntário nome={}", voluntario.getNome());
        if (voluntario.getNome() == null || voluntario.getNome().isEmpty()) {
            log.warn("Dados inválidos para salvar voluntário: nome ausente");
            throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_NAME);
        }

        // Se houver um Usuário aninhado, valide e persista primeiro
        if (voluntario.getUsuario() != null) {
            log.debug("Voluntário contém usuário aninhado email={}", voluntario.getUsuario().getEmail());
            if (voluntario.getUsuario().getEmail() == null || !voluntario.getUsuario().getEmail().contains("@")) {
                log.warn("Email inválido fornecido para usuário do voluntário: {}", voluntario.getUsuario().getEmail());
                throw new InvalidVoluntarioDataException(ConstantExceptionUtil.INVALID_VOLUNTARIO_EMAIL);
            }
            if (usuarioRepository.findByEmail(voluntario.getUsuario().getEmail()).isPresent()) {
                log.warn("Email já existente no banco: {}", voluntario.getUsuario().getEmail());
                throw new EmailAlreadyExistsException(voluntario.getUsuario().getEmail());
            }
            if (voluntario.getUsuario().getRole() == null) {
                voluntario.getUsuario().setRole(PerfisUsuariosEnum.VOLUNTARIO);
            }
            if (voluntario.getUsuario().getSenha() != null) {
                log.debug("Codificando senha para o usuário do voluntário");
                voluntario.getUsuario().setSenha(passwordEncoder.encode(voluntario.getUsuario().getSenha()));
            }
            // salve o usuário primeiro para garantir que ele tenha um id (nenhuma cascata configurada)
            try {
                usuarioRepository.save(voluntario.getUsuario());
                log.info("Usuário do voluntário salvo email={}", voluntario.getUsuario().getEmail());
            } catch (DataIntegrityViolationException dive) {
                // Em caso de condição de corrida ou violação de restrição de banco de dados, traduzir para exceção de domínio
                log.error("Falha ao salvar usuário do voluntário (possível corrida/violação de integridade) email={}", voluntario.getUsuario().getEmail());
                throw new EmailAlreadyExistsException(voluntario.getUsuario().getEmail());
            }
        }

        Voluntario saved = repository.save(voluntario);
        log.info("Voluntário salvo com sucesso id={}", saved.getId());
        return saved;
    }


    public Optional<List<Voluntario>> loadAll() {
        log.info("Carregando todos os voluntários do banco");
        return Optional.of(repository.findAll());
    }

    public Voluntario update(Long id, Voluntario voluntario) {
        log.info("Iniciando atualização do voluntário id={}", id);
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
                log.debug("Atualizada escala id={} ao remover/ajustar voluntário id={}", escala.getId(), oldVoluntario.getId());
            }

        }
        Voluntario updated = repository.save(oldVoluntario);
        log.info("Atualização concluída para voluntário id={}", updated.getId());
        return updated;
    }

    public Optional<List<Voluntario>> findVoluntariosByData(LocalDate data) {
        log.debug("Buscando voluntários disponíveis na data={}", data);
        return repository.findVoluntariosByData(data);
    }

    private void mergeDatasDisponiveis(Voluntario voluntario, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = voluntario.getDatasDisponiveis();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream().filter(date -> !datasAtuais.contains(date)).forEach(datasAtuais::add);
    }

    public boolean delete(Long id) {
        log.info("Tentativa de deletar voluntário id={}", id);
        if (repository.existsById(id)) {
            if(escalaRepository.existsByVoluntarios(Voluntario.builder().id(id).build())){
                log.warn("Não é possível deletar voluntário id={} pois ele está escalado", id);
                throw new VoluntarioIsScheduledException();
            }
            repository.deleteById(id);
            log.info("Voluntário deletado com sucesso id={}", id);
            return true;
        } else {
            log.warn("Falha ao deletar voluntário id={} - não encontrado", id);
            throw new EntityNotFoundException("Voluntário", id);
        }
    }
}
