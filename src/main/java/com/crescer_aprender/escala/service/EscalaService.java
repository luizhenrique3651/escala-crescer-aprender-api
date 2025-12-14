package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.VoluntarioNotExistException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.EscalaSpecifications;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EscalaService {

    private final EscalaRepository repository;
    private final VoluntarioRepository voluntarioRepository;

    @Autowired
    public EscalaService(EscalaRepository repository, VoluntarioRepository voluntarioRepository) {
        this.repository = repository;
        this.voluntarioRepository = voluntarioRepository;
    }

    public Page<Escala> findByFiltersPaginated(Map<String, String> filters, Pageable pageable) {
        return repository.findAll(EscalaSpecifications.byFilters(filters), pageable);
    }

    public Optional<Escala> findEscalaByMesAnoVoluntario(Integer mes, Long ano, Long voluntario) {
        Optional<Escala> retorno = repository.findEscalaByMesAnoVoluntario(mes, ano, voluntario);
        if (retorno.isPresent()) {
            return retorno;
        } else {
            throw new EntityNotFoundException("Escala não encontrada em " + mes + "/" + ano + " com o Voluntário: " + voluntario.toString());
        }
    }

    public Optional<Escala> findByAnoAndMes(LocalDate data) {
        return repository.findByAnoAndMes(data.getYear(), data.getMonthValue());
    }

    public Optional<Escala> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<List<Escala>> loadAll() {
        return Optional.of(repository.findAll());
    }

    public Escala save(Escala escala) {

        verificaSeHaEscalaCadastradaNaData(escala);

        log.info("Verificando voluntários disponíveis para as datas da escala. E criando lista de voluntários disponíveis.");
        List<Voluntario> voluntariosDisponiveis = new ArrayList<>();
        verificaVoluntariosDisponiveisEAdiciona(voluntariosDisponiveis, escala);

        log.info("Verificando se os voluntários disponíveis existem no banco de dados.");
        verificaVoluntariosEstaoNoBanco(voluntariosDisponiveis);

        adicionaAleatoriosSeNenhumVoluntario(voluntariosDisponiveis, escala);

        // garantir que não ultrapasse 8 voluntários na escala
        if (escala.getVoluntarios() != null && escala.getVoluntarios().size() > 8) {
            log.warn("Quantidade de voluntários na escala ({}) excede o limite de 8, truncando para 8.", escala.getVoluntarios().size());
            escala.setVoluntarios(escala.getVoluntarios().subList(0, 8));
        }

        return repository.save(escala);

    }

    public Escala update(Long id, Escala escala) {
        Escala oldEscala = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas()).ifPresent(dates -> mergeDatas(oldEscala, dates));
        Optional.ofNullable(escala.getVoluntarios()).ifPresent(voluntarios -> mergeVoluntarios(oldEscala, voluntarios));
        return repository.save(oldEscala);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            throw new EntityNotFoundException("Escala", id);
        }
    }

    private void mergeVoluntarios(Escala escala, List<Voluntario> novosVoluntarios) {
        List<Voluntario> voluntariosAtuais = escala.getVoluntarios();

        voluntariosAtuais.removeIf(voluntario -> !novosVoluntarios.contains(voluntario));
        novosVoluntarios.stream().filter(voluntario -> !voluntariosAtuais.contains(voluntario)).forEach(voluntariosAtuais::add);

    }

    private void mergeDatas(Escala escala, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = escala.getDatas();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream().filter(date -> !datasAtuais.contains(date)).forEach(datasAtuais::add);
    }

    // manter compatibilidade, mas preferível o método paginado
    public Optional<List<Escala>> findByFiltersWithoutPagination(Map<String, String> filters) {
        List<Escala> results = repository.findAll(EscalaSpecifications.byFilters(filters));
        return Optional.ofNullable(results == null || results.isEmpty() ? null : results);
    }

    public void verificaSeHaEscalaCadastradaNaData(Escala escala) {
        log.info("Verificando se já existe escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes());
        if (escalaExistente.isPresent()) {
            log.info("Já existe uma escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }
    }

    /*Além de verificar quais voluntários estão disponíveis nas datas, caso hajam mais de 8 disponíveis, a escolha
     * dos voluntários a serem escalados é feita aleatoriamente(só pode ter no máximo 8 voluntários na escala).*/
    public void verificaVoluntariosDisponiveisEAdiciona(List<Voluntario> voluntariosDisponiveis, Escala escala) {
        if (escala.getDatas() == null || escala.getDatas().isEmpty()) {
            log.debug("Escala sem datas definidas, pulando verificação de disponibilidade");
            return;
        }
        for (LocalDate data : escala.getDatas()) {
            Optional<List<Voluntario>> listaVoluntariosNaData = voluntarioRepository.findVoluntariosByData(data);
            if (listaVoluntariosNaData.isPresent()) {
                /*verifica se as duas listas de voluntarios tem objetos repetidos e retira de uma para
                 adicionar depois o restante na outra*/
                listaVoluntariosNaData.get().removeIf(v -> voluntariosDisponiveis.contains(v));
                //verifica se a quantidade de voluntarios ja adicionados + os da data ultrapassa 8
                if (voluntariosDisponiveis.size() + listaVoluntariosNaData.get().size() > 8) {
                    // embaralha a lista para tornar aleatória a seleção
                    Collections.shuffle(listaVoluntariosNaData.get());
                    //escala 8 voluntarios disponiveis aleatoriamente pois a lista foi embaralhada
                    voluntariosDisponiveis.addAll(listaVoluntariosNaData.get().subList(0, 8 - voluntariosDisponiveis.size()));
                } else {
                    voluntariosDisponiveis.addAll(listaVoluntariosNaData.get());
                }
            }
        }
    }

    private void verificaVoluntariosEstaoNoBanco(List<Voluntario> voluntariosDisponiveis) {
        if (voluntariosDisponiveis == null || voluntariosDisponiveis.isEmpty()) {
            log.debug("Nenhum voluntário disponível para verificar no banco");
            return;
        }

        List<Long> idsVoluntariosDisponiveis = voluntariosDisponiveis.stream()
                .map(Voluntario::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (idsVoluntariosDisponiveis.isEmpty()) {
            log.debug("IDs dos voluntários disponíveis vazios após filtragem de nulls");
            return;
        }

        Optional<List<Voluntario>> voluntariosExistentes = voluntarioRepository.findVoluntariosByIds(idsVoluntariosDisponiveis);
        if (voluntariosExistentes.isPresent()) {
            Set<Long> existentes = voluntariosExistentes.get().stream()
                    .map(Voluntario::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Long> ausentes = idsVoluntariosDisponiveis.stream()
                    .filter(id -> !existentes.contains(id))
                    .collect(Collectors.toList());

            if (!ausentes.isEmpty()) {
                log.error("Voluntários com os IDs {} não existem no banco de dados.", ausentes);
                throw new VoluntarioNotExistException(ausentes);
            }
        } else {
            log.error("Voluntários com os IDs {} não existem no banco de dados.", idsVoluntariosDisponiveis);
            throw new VoluntarioNotExistException(idsVoluntariosDisponiveis);
        }
    }

    private void adicionaAleatoriosSeNenhumVoluntario(List<Voluntario> voluntariosDisponiveis, Escala escala) {
        if (escala.getVoluntarios() == null) {
            escala.setVoluntarios(new ArrayList<>());
        }
        if (escala.getVoluntarios().isEmpty()) {
            log.info("Nenhum voluntário foi especificado na escala. Adicionando voluntários disponíveis de forma aleatória.");
            if (voluntariosDisponiveis == null || voluntariosDisponiveis.isEmpty()) {
                log.warn("Não há voluntários disponíveis para adicionar à escala");
                return;
            }
            // embaralha para aleatoriedade e limita a 8
            Collections.shuffle(voluntariosDisponiveis);
            int limit = Math.min(8, voluntariosDisponiveis.size());
            escala.getVoluntarios().addAll(voluntariosDisponiveis.subList(0, limit));
            log.info("Adicionados {} voluntários à escala", escala.getVoluntarios().size());
        }
    }
}
