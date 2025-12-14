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
        if(retorno.isPresent()){
            return retorno;
        }else{
            throw new EntityNotFoundException("Escala não encontrada em "+mes+"/"+ano+" com o Voluntário: "+voluntario.toString());
        }
    }

    public Optional<Escala> findByAnoAndMes(LocalDate data){
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

        // se foram passados dias com ids de voluntarios, converte para EscalaDia
        List<EscalaDia> dias = new ArrayList<>();
        Set<Voluntario> union = new LinkedHashSet<>();
        if (request.getDias() != null && !request.getDias().isEmpty()) {
            for (EscalaDiaRequest dr : request.getDias()) {
                EscalaDia dia = new EscalaDia();
                dia.setData(dr.getData());
                if (dr.getVoluntarios() != null && !dr.getVoluntarios().isEmpty()) {
                    // busca por ids
                    Optional<List<Voluntario>> vs = voluntarioRepository.findVoluntariosByIds(dr.getVoluntarios());
                    if (vs.isEmpty() || vs.get().size() != dr.getVoluntarios().size()) {
                        List<Long> missing = new ArrayList<>();
                        if (vs.isPresent()) {
                            Set<Long> found = vs.get().stream().map(Voluntario::getId).collect(Collectors.toSet());
                            for (Long id : dr.getVoluntarios()) if (!found.contains(id)) missing.add(id);
                        } else missing.addAll(dr.getVoluntarios());
                        throw new VoluntarioNotExistException(missing);
                    }
                    dia.setVoluntarios(vs.get());
                    union.addAll(vs.get());
                    // valida disponibilidade: cada voluntario especificado deve conter a data em datasDisponiveis
                    List<Long> notAvailable = vs.get().stream()
                            .filter(v -> v.getDatasDisponiveis() == null || !v.getDatasDisponiveis().contains(dr.getData()))
                            .map(Voluntario::getId)
                            .collect(Collectors.toList());
                    if (!notAvailable.isEmpty()) {
                        throw new InvalidVoluntarioDataException("Voluntários não disponíveis para a data " + dr.getData() + ": " + notAvailable);
                    }
                } else {
                    // se não passou ids, deixa que o método de persistência selecione baseado em disponibilidade
                    Optional<List<Voluntario>> candidatos = voluntarioRepository.findVoluntariosByData(dr.getData());
                    if (candidatos.isEmpty() || candidatos.get().size() < 4) {
                        throw new InvalidVoluntarioDataException("Menos de 4 voluntários disponíveis para a data: " + dr.getData());
                    }
                    List<Voluntario> c = new ArrayList<>(candidatos.get());
                    Collections.shuffle(c);
                    int seleciona = Math.min(8, c.size());
                    List<Voluntario> selecionados = new ArrayList<>(c.subList(0, seleciona));
                    dia.setVoluntarios(selecionados);
                    union.addAll(selecionados);
                }
                dias.add(dia);
            }
        }

        // se veio somente lista de voluntarios ids no body (legacy), resolve e adiciona à escala.voluntarios (união)
        if (request.getVoluntarios() != null && !request.getVoluntarios().isEmpty()) {
            Optional<List<Voluntario>> vs = voluntarioRepository.findVoluntariosByIds(request.getVoluntarios());
            if (vs.isEmpty() || vs.get().size() != request.getVoluntarios().size()) {
                List<Long> missing = new ArrayList<>();
                if (vs.isPresent()) {
                    Set<Long> found = vs.get().stream().map(Voluntario::getId).collect(Collectors.toSet());
                    for (Long id : request.getVoluntarios()) if (!found.contains(id)) missing.add(id);
                } else missing.addAll(request.getVoluntarios());
                throw new VoluntarioNotExistException(missing);
            }
            union.addAll(vs.get());
        }

        dias.forEach(d -> d.setEscala(escala));
        escala.setDias(dias);
        escala.setVoluntarios(new ArrayList<>(union));

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
            Set<Voluntario> union = new LinkedHashSet<>();
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
                union.addAll(selecionados);
            }
            diasGerados.forEach(d -> d.setEscala(escala));
            escala.setDias(diasGerados);
            if (escala.getVoluntarios() == null || escala.getVoluntarios().isEmpty()) {
                escala.setVoluntarios(new ArrayList<>(union));
            }
        }

        // verifica existência dos voluntários da união
        List<Long> ids = new ArrayList<>();
        if (escala.getVoluntarios() != null) ids = escala.getVoluntarios().stream().map(Voluntario::getId).collect(Collectors.toList());
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
        Escala oldEscala = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas())
                .ifPresent(dates -> {
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

                        EscalaDia dia = EscalaDia.builder()
                                .data(data)
                                .voluntarios(selecionados)
                                .build();
                        diasRecriados.add(dia);
                        voluntariosUnion.addAll(selecionados);
                    }
                    diasRecriados.forEach(d -> d.setEscala(oldEscala));
                    oldEscala.setDias(diasRecriados);

                    if (oldEscala.getVoluntarios() == null || oldEscala.getVoluntarios().isEmpty()) {
                        oldEscala.setVoluntarios(new ArrayList<>(voluntariosUnion));
                    }
                });
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
        novasDatas.stream()
                .filter(date -> !datasAtuais.contains(date))
                .forEach(datasAtuais::add);
    }

    // manter compatibilidade, mas preferível o método paginado
    public Optional<List<Escala>> findByFiltersWithoutPagination(Map<String, String> filters) {
        List<Escala> results = repository.findAll(EscalaSpecifications.byFilters(filters));
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results);
    }

    public void verificaSeHaEscalaCadastradaNaData(Escala escala){
        log.info("Verificando se já existe escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes());
        if (escalaExistente.isPresent()) {
            log.info("Já existe uma escala cadastrada para o ano {} e mês {}", escala.getAno(), escala.getMes());
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }

    }

}
