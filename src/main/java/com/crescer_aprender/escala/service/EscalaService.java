package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.EscalaDia;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.InvalidVoluntarioDataException;
import com.crescer_aprender.escala.exception.VoluntarioNotExistException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.EscalaSpecifications;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import com.crescer_aprender.escala.dto.EscalaCreateRequest;
import com.crescer_aprender.escala.dto.EscalaDiaRequest;
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
        // manter compatibilidade: antiga implementação permanece
        return saveEscalaEntity(escala);
    }

    // novo: salvar a partir de request DTO (recomendado para endpoints)
    public Escala saveFromRequest(EscalaCreateRequest request) {
        Escala escala = new Escala();
        escala.setMes(request.getMes());
        escala.setAno(request.getAno());
        escala.setDatas(request.getDatas());

        // se foram passados dias com ids de voluntarios, converte EscalaDiaRequest para EscalaDia
        List<EscalaDia> dias = new ArrayList<>();
        if (request.getDias() != null && !request.getDias().isEmpty()) {
            for (EscalaDiaRequest diaRequest : request.getDias()) {
                EscalaDia escalaDia = new EscalaDia();
                escalaDia.setData(diaRequest.getData());
                if (diaRequest.getVoluntarios() != null && !diaRequest.getVoluntarios().isEmpty()) {
                    // busca por ids
                    Optional<List<Voluntario>> voluntariosNoBanco = voluntarioRepository.findVoluntariosByIds(diaRequest.getVoluntarios());
                    validaVoluntariosExistentes(diaRequest, voluntariosNoBanco);
                    escalaDia.setVoluntarios(voluntariosNoBanco.get());

                    validaDisponibilidadeVoluntarios(diaRequest, voluntariosNoBanco);
                } else {
                    // se não passou ids, deixa que o método de persistência selecione baseado em disponibilidade
                    escalaVoluntariosAleatoriosDisponiveis(diaRequest, escalaDia);
                }
                dias.add(escalaDia);
            }
        }

        dias.forEach(d -> d.setEscala(escala));
        escala.setDias(dias);
        // removido campo legado escala.voluntarios; alocação por dia está em escala.dias

        return saveEscalaEntity(escala);
    }

    // extrai a implementação do save anterior para reuso
    private Escala saveEscalaEntity(Escala escala) {
        // a implementação anterior transformada aqui (valida existência de escala, salva dias já preparados, etc.)
        verificaSeHaEscalaCadastradaNaData(escala);
        // se os dias já foram pré-populados, valida disponibilidade mínima por dia
        if (escala.getDias() != null && !escala.getDias().isEmpty()) {
            for (EscalaDia d : escala.getDias()) {
                if (d.getVoluntarios() == null || d.getVoluntarios().size() < 4) {
                    throw new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + d.getData());
                }
            }
        } else {
            // compatibilidade: se não houver dias preenchidos, seleciona voluntários por data (4..8) baseado nas disponibilidades
            List<EscalaDia> diasGerados = new ArrayList<>();
            if (escala.getDatas() == null || escala.getDatas().isEmpty()) {
                throw new InvalidVoluntarioDataException("A escala deve conter pelo menos uma data.");
            }
            for (LocalDate data : escala.getDatas()) {
                Optional<List<Voluntario>> candidatosOpt = voluntarioRepository.findVoluntariosByData(data);
                if (candidatosOpt.isEmpty() || candidatosOpt.get().size() < 4) {
                    throw new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + data);
                }
                List<Voluntario> candidatos = new ArrayList<>(candidatosOpt.get());
                Collections.shuffle(candidatos);
                int seleciona = Math.min(8, candidatos.size());
                List<Voluntario> selecionados = new ArrayList<>(candidatos.subList(0, seleciona));

                EscalaDia dia = new EscalaDia();
                dia.setData(data);
                dia.setVoluntarios(selecionados);
                diasGerados.add(dia);
            }
            diasGerados.forEach(d -> d.setEscala(escala));
            escala.setDias(diasGerados);
            // removido campo legado escala.voluntarios; diasGerados já contém as alocações por dia
        }

        // verifica existência dos voluntários da união
        // coletar ids de voluntários a partir de escala.dias (união)
        List<Long> ids = new ArrayList<>();
        if (escala.getDias() != null) {
            ids = escala.getDias().stream().filter(d -> d.getVoluntarios() != null).flatMap(d -> d.getVoluntarios().stream()).map(Voluntario::getId).distinct().collect(Collectors.toList());
        }
        if (!ids.isEmpty()) {
            Optional<List<Voluntario>> voluntariosExistentes = voluntarioRepository.findVoluntariosByIds(ids);
            if (voluntariosExistentes.isEmpty() || voluntariosExistentes.get().size() != ids.size()) {
                List<Long> missing = new ArrayList<>();
                if (voluntariosExistentes.isPresent()) {
                    Set<Long> found = voluntariosExistentes.get().stream().map(Voluntario::getId).collect(Collectors.toSet());
                    for (Long id : ids) if (!found.contains(id)) missing.add(id);
                } else missing.addAll(ids);
                throw new VoluntarioNotExistException(missing);
            }
        }

        // relaciona dias à escala (owner)
        if (escala.getDias() != null) escala.getDias().forEach(d -> d.setEscala(escala));
        return repository.save(escala);

    }

    public Escala update(Long id, Escala escala) {
        Escala oldEscala = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas()).ifPresent(dates -> {
            mergeDatas(oldEscala, dates);
            // após mesclar as datas, recria os dias conforme disponibilidades
            List<EscalaDia> diasRecriados = new ArrayList<>();
            Set<Voluntario> voluntariosUnion = new LinkedHashSet<>();
            for (LocalDate data : oldEscala.getDatas()) {
                Optional<List<Voluntario>> listaVoluntariosNaData = voluntarioRepository.findVoluntariosByData(data);
                if (listaVoluntariosNaData.isEmpty() || listaVoluntariosNaData.get().size() < 4) {
                    throw new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + data);
                }
                List<Voluntario> candidatos = new ArrayList<>(listaVoluntariosNaData.get());
                Collections.shuffle(candidatos);
                int seleciona = Math.min(8, candidatos.size());
                List<Voluntario> selecionados = new ArrayList<>(candidatos.subList(0, seleciona));

                EscalaDia dia = EscalaDia.builder().data(data).voluntarios(selecionados).build();
                diasRecriados.add(dia);
                voluntariosUnion.addAll(selecionados);
            }
            diasRecriados.forEach(d -> d.setEscala(oldEscala));
            if(escala.getDias() != null) {
                oldEscala.getDias().clear();
            }else{
                oldEscala.setDias(new ArrayList<>());
            }
                oldEscala.getDias().addAll(diasRecriados);

            // removido campo legado escala.voluntarios; a alocação fica em oldEscala.dias
        });
        // antigo suporte a mergeVoluntarios removido; use apenas escala.dias para atualizar alocações por dia
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


    private void mergeDatas(Escala escala, List<LocalDate> novasDatas) {
        List<LocalDate> datasAtuais = escala.getDatas();

        datasAtuais.removeIf(date -> !novasDatas.contains(date));
        novasDatas.stream().filter(date -> !datasAtuais.contains(date)).forEach(datasAtuais::add);
    }

    // manter compatibilidade, mas preferível o método paginado
    public Optional<List<Escala>> findByFiltersWithoutPagination(Map<String, String> filters) {
        List<Escala> results = repository.findAll(EscalaSpecifications.byFilters(filters));
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results);
    }

    public void verificaSeHaEscalaCadastradaNaData(Escala escala) {
        log.info("Verificando se já existe escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes());
        if (escalaExistente.isPresent()) {
            log.info("Já existe uma escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }
    }

    private static void validaVoluntariosExistentes(EscalaDiaRequest diaRequest, Optional<List<Voluntario>> voluntariosNoBanco) {
        log.info("Verificando se todos os voluntarios do EscalaDiaRequest : {} existem no banco", diaRequest.getData());
        if (voluntariosNoBanco.isEmpty() || voluntariosNoBanco.get().size() != diaRequest.getVoluntarios().size()) {
            List<Long> missing = new ArrayList<>();
            if (voluntariosNoBanco.isPresent()) {
                Set<Long> found = voluntariosNoBanco.get().stream().map(Voluntario::getId).collect(Collectors.toSet());
                for (Long id : diaRequest.getVoluntarios()) if (!found.contains(id)) missing.add(id);
            } else missing.addAll(diaRequest.getVoluntarios());
            throw new VoluntarioNotExistException(missing);
        }
        log.info("Voluntarios para o EscalaDiaRequest: {} válidos.", diaRequest.getData());
    }

    private static void validaDisponibilidadeVoluntarios(EscalaDiaRequest diaRequest, Optional<List<Voluntario>> voluntariosNoBanco) {
        log.info("Verificando se todos voluntários passados para o EscalaDiaRequest : {} estão de fato disponíveis para o dia.", diaRequest.getData());
        // valida disponibilidade: cada voluntario especificado deve conter a data em datasDisponiveis
        List<Long> notAvailable = voluntariosNoBanco.get().stream().filter(v -> v.getDatasDisponiveis() == null || !v.getDatasDisponiveis().contains(diaRequest.getData())).map(Voluntario::getId).toList();
        if (!notAvailable.isEmpty()) {
            throw new InvalidVoluntarioDataException("Voluntários não disponíveis para a data " + diaRequest.getData() + ": " + notAvailable);
        }
        log.info("Voluntários disponíveis para o dia {}", diaRequest.getData());
    }

    private void escalaVoluntariosAleatoriosDisponiveis(EscalaDiaRequest diaRequest, EscalaDia escalaDia) {
        Optional<List<Voluntario>> candidatos = voluntarioRepository.findVoluntariosByData(diaRequest.getData());
        if (candidatos.isEmpty() || candidatos.get().size() < 4) {
            throw new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + diaRequest.getData());
        }
        List<Voluntario> voluntariosCandidatos = new ArrayList<>(candidatos.get());
        Collections.shuffle(voluntariosCandidatos);
        int seleciona = Math.min(8, voluntariosCandidatos.size());
        List<Voluntario> selecionados = new ArrayList<>(voluntariosCandidatos.subList(0, seleciona));
        escalaDia.setVoluntarios(selecionados);
    }

}
