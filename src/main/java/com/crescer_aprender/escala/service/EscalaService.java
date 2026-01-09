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
import org.springframework.transaction.annotation.Transactional;

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


    // novo: salvar a partir de request DTO (recomendado para endpoints)
    @Transactional
    public Escala saveFromRequest(EscalaCreateRequest request) {
        Escala escala = new Escala();
        escala.setMes(request.getMes());
        escala.setAno(request.getAno());
        escala.setDatas(request.getDatas());

        verificaSeHaEscalaCadastradaNaData(escala);

        if(request.getIncluirVoluntariosAutomaticamente()) {
            populaEscalaComDias(escala, request);
        }
        return repository.save(escala);
    }

    @Transactional
    public Escala update(Long id, Escala escala) {
        log.info("Iniciando atualização da Escala ={}", escala);
        Escala oldEscala = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        recriaDiasParaUpdate(oldEscala, escala);

        // antigo suporte a mergeVoluntarios removido; use apenas escala.dias para atualizar alocações por dia
        log.info("Finalizando atualização da Escala ={}", escala);
        return repository.save(oldEscala);
    }

    public void recriaDiasParaUpdate(Escala oldEscala, Escala escala){
        Optional.ofNullable(escala.getDatas()).ifPresent(dates -> {
            mergeDatas(oldEscala, dates);
            // após mesclar as datas, recria os dias conforme disponibilidades
            List<EscalaDia> diasRecriados = new ArrayList<>();
            for (LocalDate data : oldEscala.getDatas()) {
                List<Voluntario> listaVoluntariosNaData = voluntarioRepository.findVoluntariosByData(data).orElse(Collections.emptyList());
                if (listaVoluntariosNaData.isEmpty() || listaVoluntariosNaData.size() < 4) {
                    log.warn("Menos de 4 voluntários disponíveis para a data: {}", data);
                }
                List<Voluntario> candidatos = new ArrayList<>(listaVoluntariosNaData);
                Collections.shuffle(candidatos);
                int seleciona = Math.min(8, candidatos.size());
                List<Voluntario> selecionados = new ArrayList<>(candidatos.subList(0, seleciona));

                EscalaDia dia = EscalaDia.builder().data(data).voluntarios(selecionados).build();
                diasRecriados.add(dia);
            }
            diasRecriados.forEach(d -> d.setEscala(oldEscala));
            // mantém a mesma instância de coleção gerenciada pelo JPA para evitar problemas com orphanRemoval
            if (oldEscala.getDias() == null) {
                oldEscala.setDias(new ArrayList<>());
            } else {
                oldEscala.getDias().clear();
            }
            oldEscala.getDias().addAll(diasRecriados);

            // removido campo legado escala.voluntarios; a alocação fica em oldEscala.dias
        });
    }
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            throw new EntityNotFoundException("Escala", id);
        }
    }

    @Transactional
    public Escala populaEscalaComVoluntarios(Long idEscala){
        // carregar a instância gerenciada e modificá-la diretamente para evitar conflitos de coleção (orphanRemoval)
        Escala escala = repository.findById(idEscala).orElseThrow(() -> new EntityNotFoundException("Escala", idEscala));
        populaEscalaComDias(escala, EscalaCreateRequest.of(escala));
        // Como estamos em uma transação, simplesmente salvar a entidade gerenciada mantém a integridade das coleções
        return repository.save(escala);
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

    private void populaEscalaComDias(Escala escala, EscalaCreateRequest request) {
        log.info("Iniciando população da escala com dias e voluntários... | Escala ={}", escala);
        // Metodo que verifica se a request foram passados dias completos com voluntários
        List<EscalaDia> dias = new ArrayList<>();
        if (request.getDias() != null && !request.getDias().isEmpty()) {
            log.info("Percorrendo dias passados pela request, para maepar em EscalaDia e adicionar os voluntários...");
            for (EscalaDiaRequest diaRequest : request.getDias()) {
                log.info("Processando EscalaDiaRequest para data = {}", diaRequest.getData());
                EscalaDia escalaDia = new EscalaDia();
                escalaDia.setData(diaRequest.getData());
                if (diaRequest.getVoluntarios() != null && !diaRequest.getVoluntarios().isEmpty()) {
                    log.info("Processando Voluntarios para data = {}", diaRequest.getVoluntarios());
                    // busca por ids
                    Optional<List<Voluntario>> voluntariosNoBanco = voluntarioRepository.findVoluntariosByIds(diaRequest.getVoluntarios());
                    if (voluntariosNoBanco.isPresent()) {
                        log.info("Validando voluntarios para data = {}", diaRequest.getVoluntarios());
                        validaVoluntariosExistentes(diaRequest, voluntariosNoBanco);
                        validaDisponibilidadeVoluntarios(diaRequest, voluntariosNoBanco);
                        escalaDia.setVoluntarios(voluntariosNoBanco.get());
                        log.info("Voluntarios validados e atribuídos para data = {}", diaRequest.getData());

                    }
                } else {
                    // se não passou ids, deixa que o método de persistência selecione baseado em disponibilidade
                    log.info("Request não possui voluntários específicos para a data ={}. Selecionando aleatoriamente entre os disponíveis.", diaRequest.getData());
                    escalaVoluntariosAleatoriosDisponiveis(diaRequest, escalaDia);
                    log.info("Voluntarios aleatórios atribuídos para data = {}", diaRequest.getData());
                }
                dias.add(escalaDia);
            }
            dias.forEach(d -> d.setEscala(escala));
            // ao invés de substituir a coleção gerenciada pelo JPA, muta a instância existente para evitar problemas com orphanRemoval
            if (escala.getDias() == null) {
                escala.setDias(new ArrayList<>());
            } else {
                escala.getDias().clear();
            }
            escala.getDias().addAll(dias);
        } else {
            criaDiasComVoluntariosDisponiveis(escala);
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
        List<Voluntario> voluntariosList = voluntariosNoBanco.orElse(Collections.emptyList());
        List<Long> notAvailable = voluntariosList.stream().filter(v -> v.getDatasDisponiveis() == null || !v.getDatasDisponiveis().contains(diaRequest.getData())).map(Voluntario::getId).toList();
        if (!notAvailable.isEmpty()) {
            throw new InvalidVoluntarioDataException("Voluntários não disponíveis para a data " + diaRequest.getData() + ": " + notAvailable);
        }
        log.info("Todos Voluntários passados estão disponíveis para o dia {}", diaRequest.getData());
    }

    private void escalaVoluntariosAleatoriosDisponiveis(EscalaDiaRequest diaRequest, EscalaDia escalaDia) {
        List<Voluntario> candidatos = voluntarioRepository.findVoluntariosByData(diaRequest.getData()).orElse(Collections.emptyList());
        if (candidatos.isEmpty()) {
            InvalidVoluntarioDataException e = new InvalidVoluntarioDataException("Não há voluntarios disponíveis para a data =" + diaRequest.getData());
            log.error(e.getMessage());
            throw e;
        }
        if (candidatos.size() < 4) {
            log.warn("Poucos voluntários para o dia = {}", diaRequest.getData());
        }
        List<Voluntario> voluntariosCandidatos = new ArrayList<>(candidatos);
        Collections.shuffle(voluntariosCandidatos);
        int seleciona = Math.min(8, voluntariosCandidatos.size());
        List<Voluntario> selecionados = new ArrayList<>(voluntariosCandidatos.subList(0, seleciona));
        escalaDia.setVoluntarios(selecionados);
    }

    private void criaDiasComVoluntariosDisponiveis(Escala escala) {
        // compatibilidade: se não houver dias preenchidos, seleciona voluntários por data (4..8) baseado nas disponibilidades
        List<EscalaDia> diasGerados = new ArrayList<>();
        if (escala.getDatas() == null || escala.getDatas().isEmpty()) {
            throw new InvalidVoluntarioDataException("A escala deve conter pelo menos uma data.");
        }
        for (LocalDate data : escala.getDatas()) {
            List<Voluntario> candidatos = new ArrayList<>(voluntarioRepository.findVoluntariosByData(data).orElse(Collections.emptyList()));
            if (candidatos.isEmpty() || candidatos.size() < 4) {
                log.warn("Dia gerado com poucos voluntários disponíveis para serem escalados | dia ={} ", data, new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + data));
            }
            Collections.shuffle(candidatos);
            int seleciona = Math.min(8, candidatos.size());
            List<Voluntario> selecionados = new ArrayList<>(candidatos.subList(0, seleciona));

            EscalaDia dia = new EscalaDia();
            dia.setData(data);
            dia.setVoluntarios(selecionados);
            diasGerados.add(dia);
        }
        diasGerados.forEach(d -> d.setEscala(escala));
        // ao invés de substituir a coleção gerenciada pelo JPA, muta a instância existente para evitar problemas com orphanRemoval
        if (escala.getDias() == null) {
            escala.setDias(new ArrayList<>());
        } else {
            escala.getDias().clear();
        }
        escala.getDias().addAll(diasGerados);
    }
}
