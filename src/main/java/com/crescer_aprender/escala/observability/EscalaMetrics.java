package com.crescer_aprender.escala.observability;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class EscalaMetrics {

    private final Counter criadas;
    private final Counter deletadas;
    private final Counter criacaoFalha;
    private final Timer   geracaoTimer;

    public EscalaMetrics(MeterRegistry registry) {
        criadas = Counter.builder("escala.criadas.total")
                .description("Total de escalas criadas com sucesso")
                .tag("dominio", "escala").register(registry);

        deletadas = Counter.builder("escala.deletadas.total")
                .description("Total de escalas deletadas")
                .tag("dominio", "escala").register(registry);

        criacaoFalha = Counter.builder("escala.criacao.falha.total")
                .description("Falhas na criação de escala")
                .tag("dominio", "escala").register(registry);

        geracaoTimer = Timer.builder("escala.geracao.duracao")
                .description("Duração da geração automática de escala")
                .tag("dominio", "escala")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .register(registry);
    }

    public void incrementCriadas()      { criadas.increment(); }
    public void incrementDeletadas()    { deletadas.increment(); }
    public void incrementCriacaoFalha() { criacaoFalha.increment(); }

    public Timer.Sample iniciarTimer()              { return Timer.start(); }
    public void finalizarTimer(Timer.Sample sample) { sample.stop(geracaoTimer); }
}
