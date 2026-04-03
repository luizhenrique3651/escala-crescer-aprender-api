# 📊 Guia: Criar Novas Métricas com OpenTelemetry

## Tipos de Instrumentos

| Instrumento           | Quando Usar                          | Exemplo                    |
|-----------------------|--------------------------------------|----------------------------|
| `Counter`             | Valor que só cresce                  | criações, erros, logins    |
| `Gauge`               | Estado atual (sobe e desce)          | voluntários ativos         |
| `Timer`               | Duração de operações + histograma    | tempo de geração de escala |
| `DistributionSummary` | Distribuição de valores numéricos    | tamanho de payload         |

## Convenção de Nomenclatura

```
<dominio>.<objeto>.<acao>.<unidade>
```

Exemplos:
- `escala.criadas.total`
- `voluntario.ativos.total`
- `auth.login.sucesso.total`
- `escala.geracao.duracao`

## Como Criar um Novo Counter

```java
@Component
public class MeuDominioMetrics {

    private final Counter minhaAcao;

    public MeuDominioMetrics(MeterRegistry registry) {
        minhaAcao = Counter.builder("meu.dominio.acao.total")
                .description("Descrição da métrica")
                .tag("dominio", "meu-dominio")
                .register(registry);
    }

    public void incrementMinhaAcao() { minhaAcao.increment(); }
}
```

## Como Criar um Gauge

```java
private final AtomicInteger valorAtual = new AtomicInteger(0);

Gauge.builder("meu.dominio.valor.atual", valorAtual, AtomicInteger::get)
        .description("Valor atual do recurso")
        .tag("dominio", "meu-dominio")
        .register(registry);

public void setValorAtual(int total) { valorAtual.set(total); }
```

## Como Criar um Timer

```java
private final Timer meuTimer;

meuTimer = Timer.builder("meu.dominio.operacao.duracao")
        .description("Duração da operação")
        .tag("dominio", "meu-dominio")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .minimumExpectedValue(Duration.ofMillis(10))
        .maximumExpectedValue(Duration.ofSeconds(10))
        .register(registry);

// Uso:
Timer.Sample sample = Timer.start();
try {
    // ... operação ...
} finally {
    sample.stop(meuTimer);
}
```

## Onde Registrar as Métricas

Coloque a classe de métricas no pacote `com.crescer_aprender.escala.observability`
e injete-a via construtor no `@Service` ou `@Component` correspondente.

## Consultando no Prometheus

Após iniciar a stack com `docker compose up`, acesse:
- **Prometheus UI**: http://localhost:9090
- **Métricas raw**: http://localhost:8080/actuator/prometheus

Exemplo de query PromQL:
```
rate(escala_criadas_total[5m])
```
