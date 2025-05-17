package com.crescer_aprender.escala.controller;

import com.crescer_aprender.escala.entity.Voluntario;
import com.crescer_aprender.escala.service.VoluntarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/voluntario")
public class VoluntarioController {

    VoluntarioService service;
    @Autowired
    public VoluntarioController(VoluntarioService service) {
        this.service = service;
    }

    @GetMapping("/loadAll")
    public Optional<List<Voluntario>> getAllVoluntarios() {
        return service.loadAll();
    }
    @PostMapping("/create")
    public Voluntario save(Voluntario voluntario){
        return service.save(voluntario);
    }
    @PutMapping("/update")
    public Optional<Voluntario> update(Long id, Voluntario voluntario){
        return Optional.of(service.update(id, voluntario));
    }
    @DeleteMapping("/delete")
    public Boolean delete(Long id){
       return service.delete(id);
    }

}
