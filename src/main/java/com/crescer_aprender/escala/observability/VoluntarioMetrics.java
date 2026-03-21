package com.crescer_aprender.escala.observability;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VoluntarioMetrics {

    private final Counter cadastrados;
    private final Counter deletados;
    private final AtomicInteger ativos = new AtomicInteger(0);

    public VoluntarioMetrics(MeterRegistry registry) {
        cadastrados = Counter.builder("voluntario.cadastrados.total")
                .description("Total de voluntários cadastrados")
                .tag("dominio", "voluntario").register(registry);

        deletados = Counter.builder("voluntario.deletados.total")
                .description("Total de voluntários removidos")
                .tag("dominio", "voluntario").register(registry);

        Gauge.builder("voluntario.ativos.total", ativos, AtomicInteger::get)
                .description("Voluntários ativos na plataforma")
                .tag("dominio", "voluntario").register(registry);
    }

    public void incrementCadastrados() { cadastrados.increment(); ativos.incrementAndGet(); }
    public void incrementDeletados()   { deletados.increment();   ativos.decrementAndGet(); }
    public void setAtivos(int total)   { ativos.set(total); }
}
