package com.crescer_aprender.escala.service;

import com.crescer_aprender.escala.entity.Escala;
import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.exception.EntityNotFoundException;
import com.crescer_aprender.escala.exception.EscalaAlreadyExistsException;
import com.crescer_aprender.escala.exception.VoluntarioNotExistException;
import com.crescer_aprender.escala.repository.EscalaRepository;
import com.crescer_aprender.escala.repository.EscalaSpecifications;
import com.crescer_aprender.escala.repository.VoluntarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<Escala> escalaExistente = repository.findByAnoAndMes(escala.getAno().intValue(), escala.getMes());
        if (escalaExistente.isPresent()) {
            throw new EscalaAlreadyExistsException(escala.getAno(), escala.getMes());
        }
        List<Voluntario> voluntariosDisponiveis = new ArrayList<>();
        for (LocalDate data : escala.getDatas()) {
            Optional<List<Voluntario>> listaVoluntarios = voluntarioRepository.findVoluntariosByData(data);
            if (listaVoluntarios.isPresent()) {
                /*verifica se as duas listas de voluntarios tem objetos repetidos e retira de uma para
                 adicionar depois o restante na outra*/
                listaVoluntarios.get().removeIf(v -> voluntariosDisponiveis.contains(v));
                if (voluntariosDisponiveis.size() + listaVoluntarios.get().size() > 8) {
                    voluntariosDisponiveis.addAll(listaVoluntarios.get().subList(0, 8 - voluntariosDisponiveis.size()));
                } else {
                    voluntariosDisponiveis.addAll(listaVoluntarios.get());
                }
            }

        }

        List<Long> ids = voluntariosDisponiveis.stream()
                .map(Voluntario::getId)
                .collect(Collectors.toList());

        Optional<List<Voluntario>> voluntariosExistentes = voluntarioRepository.findVoluntariosByIds(ids);
        if(voluntariosExistentes.isPresent()) {
            if (ids.size() > voluntariosExistentes.get().size()) {
                ids.removeIf(v -> (voluntariosExistentes.get().stream().map(Voluntario::getId).equals(v)));
                throw new VoluntarioNotExistException(ids);
            }
        }else{
            throw new VoluntarioNotExistException(ids);
        }

        if (escala.getVoluntarios() == null || escala.getVoluntarios().isEmpty()) {
            escala.getVoluntarios().addAll(voluntariosDisponiveis);
        }
        return repository.save(escala);

    }

    public Escala update(Long id, Escala escala) {
        Escala oldEscala = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Escala", id));

        Optional.ofNullable(escala.getAno()).ifPresent(oldEscala::setAno);
        Optional.ofNullable(escala.getMes()).ifPresent(oldEscala::setMes);
        Optional.ofNullable(escala.getDatas())
                .ifPresent(dates -> mergeDatas(oldEscala, dates));
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
        return Optional.ofNullable(results == null || results.isEmpty() ? null : results);
    }

}
