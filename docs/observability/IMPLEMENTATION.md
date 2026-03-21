# 🔭 Implementação de Observabilidade — OpenTelemetry

## Stack

| Componente        | Função                                      |
|-------------------|---------------------------------------------|
| OTel SDK          | Coleta traces, metrics e logs na aplicação  |
| OTel Collector    | Recebe, processa e exporta telemetria        |
| Jaeger            | Visualização de traces distribuídos          |
| Prometheus        | Armazenamento e consulta de métricas         |
| Spring Actuator   | Endpoint `/actuator/prometheus`              |

## Arquitetura

```
Spring Boot App
      │ OTLP/gRPC (4317)
      ▼
OTel Collector
      ├──► Jaeger     → http://localhost:16686
      └──► Prometheus → http://localhost:9090
```

## Passo 1 — Dependências
BOM `opentelemetry-instrumentation-bom:2.10.0` gerencia versões.
Starters adicionados: `opentelemetry-spring-boot-starter`, `opentelemetry-exporter-otlp`,
`micrometer-registry-otlp`, `spring-boot-starter-actuator`.

## Passo 2 — Configuração
Propriedades `otel.*` no `application.properties` configuram serviço, endpoint OTLP e exportadores.

## Passo 3 — `ObservabilityConfig`
Classe `@Configuration` que registra binders de JVM (memória, GC, threads) no `MeterRegistry`.

## Passo 4 — Pacote `observability/`
Três componentes `@Component`, um por domínio:
- `EscalaMetrics` — counters e timer para criação/deleção de escalas
- `VoluntarioMetrics` — counters e gauge de voluntários ativos
- `AuthMetrics` — counters para login e acesso negado

## Passo 5 — Instrumentação dos Services
Métricas injetadas via construtor em `EscalaService`, `VoluntarioService`,
`AuthController` e `CustomAccessDeniedHandler`.

## Passo 6 — Infraestrutura Docker
Adicionados `otel-collector`, `jaeger` e `prometheus` ao `docker-compose.yml`
com configs em `docker/`.

## Verificação

| O quê                     | Onde                                     |
|---------------------------|------------------------------------------|
| Traces HTTP               | http://localhost:16686 (Jaeger UI)       |
| Métricas de negócio       | http://localhost:9090 (Prometheus)       |
| Health check              | http://localhost:8080/actuator/health    |
| Métricas raw              | http://localhost:8080/actuator/prometheus|
