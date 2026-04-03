# 🔭 Implementação de Observabilidade — OpenTelemetry

## O que é Observabilidade? Por que precisamos dela?

Imagine que a sua aplicação está rodando em produção e, de repente, os usuários começam a reclamar que ela está lenta ou que alguma funcionalidade não está funcionando. Sem observabilidade, você fica "no escuro" — não sabe onde está o problema, o que aconteceu antes do erro, ou quanto tempo cada operação está demorando.

**Observabilidade** é a capacidade de entender o que está acontecendo _dentro_ do sistema a partir dos dados que ele gera para fora. Ela se apoia em três pilares:

| Pilar       | O que é                                                               | Exemplo prático                                              |
|-------------|-----------------------------------------------------------------------|--------------------------------------------------------------|
| **Traces**  | O "rastro" completo de uma requisição, do início ao fim               | "O login do usuário levou 320ms: 5ms na autenticação, 315ms no banco" |
| **Métricas**| Números coletados ao longo do tempo que mostram tendências            | "Foram criadas 47 escalas na última hora"                    |
| **Logs**    | Mensagens textuais registradas pela aplicação em momentos específicos | "WARN: Poucos voluntários disponíveis para a data 2024-06-15"|

Sem esses dados, depurar um problema em produção é como tentar resolver um quebra-cabeça com metade das peças faltando.

---

## O que é OpenTelemetry?

**OpenTelemetry (OTel)** é um padrão aberto e neutro de fornecedor para coletar traces, métricas e logs de aplicações. Antes do OpenTelemetry, cada ferramenta de monitoramento (Datadog, New Relic, Jaeger etc.) tinha seu próprio jeito de instrumentar o código, o que gerava dependência e retrabalho quando a empresa queria trocar de ferramenta.

Com o OpenTelemetry, você instrumenta o código _uma única vez_ usando a API padrão e, depois, escolhe livremente para qual ferramenta os dados serão enviados, sem mudar nada no código da aplicação.

```
Código da Aplicação
   │  (instrumentado com OTel API — padrão aberto)
   ▼
OTel Collector (roteador central de telemetria)
   ├──► Jaeger      (visualizar traces)
   ├──► Prometheus  (armazenar métricas)
   └──► Qualquer outra ferramenta no futuro
```

---

## Visão Geral da Stack Implementada

| Componente            | Papel na arquitetura                                                        | URL local                         |
|-----------------------|-----------------------------------------------------------------------------|-----------------------------------|
| **OTel SDK**          | Biblioteca dentro do Spring Boot que coleta traces, métricas e logs         | (interno à aplicação)             |
| **OTel Collector**    | Serviço intermediário que recebe os dados, processa e os distribui          | gRPC porta `4317`, HTTP porta `4318` |
| **Jaeger**            | Interface visual para explorar os traces (rastrear uma requisição completa) | http://localhost:16686            |
| **Prometheus**        | Banco de séries temporais que armazena métricas e permite consultas         | http://localhost:9090             |
| **Spring Actuator**   | Endpoint da aplicação que expõe métricas no formato que o Prometheus entende| http://localhost:8080/actuator/prometheus |

### Fluxo de dados

```
┌──────────────────────────────────────┐
│         Spring Boot App              │
│  (OTel SDK instrumenta automatica-   │
│   mente HTTP, JDBC, logs, etc.)      │
└───────────────┬──────────────────────┘
                │ OTLP/gRPC (porta 4317)
                ▼
┌──────────────────────────────────────┐
│          OTel Collector              │
│  (recebe, filtra, processa e roteia) │
└───────┬──────────────────────────────┘
        │                    │
        ▼                    ▼
┌──────────────┐    ┌─────────────────┐
│    Jaeger    │    │   Prometheus    │
│  (traces)    │    │   (métricas)    │
└──────────────┘    └─────────────────┘
```

---

## Passo 1 — Dependências no `pom.xml`

**Por que precisamos adicionar dependências?**

O Spring Boot não vem com observabilidade pronta. Precisamos adicionar bibliotecas que ensinam a aplicação a coletar e exportar dados de telemetria.

### O BOM (Bill of Materials)

```xml
<dependencyManagement>
  <dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-bom</artifactId>
    <version>2.10.0</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencyManagement>
```

**Por que um BOM?** O BOM é como uma "lista de compras com preços fixos". Ele define de forma centralizada qual versão de cada biblioteca OTel usar. Sem ele, você teria que especificar a versão em cada dependência individualmente, e versões incompatíveis entre si causariam erros difíceis de depurar em tempo de execução.

### As dependências

| Dependência                          | O que faz                                                                                          |
|--------------------------------------|----------------------------------------------------------------------------------------------------|
| `opentelemetry-spring-boot-starter`  | Auto-instrumentação: intercepta chamadas HTTP, queries SQL e cria traces automaticamente           |
| `opentelemetry-exporter-otlp`        | Permite que a aplicação envie dados pelo protocolo OTLP para o Collector                           |
| `micrometer-registry-otlp`           | Integra o sistema de métricas do Spring (Micrometer) com o OTel, para que métricas também sejam exportadas |
| `spring-boot-starter-actuator`       | Adiciona endpoints de gestão (`/actuator/health`, `/actuator/prometheus`) à aplicação              |

**Por que o Actuator?** O Prometheus funciona em modo "pull" — ele periodicamente _busca_ as métricas da aplicação. O Actuator expõe o endpoint `/actuator/prometheus` que o Prometheus consulta. Sem ele, o Prometheus não conseguiria coletar as métricas da aplicação.

---

## Passo 2 — Configuração no `application.properties`

As propriedades abaixo informam ao OTel SDK _quem_ é a aplicação e _para onde_ ele deve enviar os dados.

```properties
# Identificação da aplicação nos sistemas de monitoramento
otel.service.name=escala-crescer-aprender-api
otel.service.version=@project.version@

# Endereço do OTel Collector (dentro do Docker, o hostname é "otel-collector")
otel.exporter.otlp.endpoint=http://otel-collector:4317
otel.exporter.otlp.protocol=grpc

# Habilita o envio de traces, métricas e logs via OTLP
otel.traces.exporter=otlp
otel.metrics.exporter=otlp
otel.logs.exporter=otlp

# Propagação de contexto: permite que o trace ID viaje entre serviços via HTTP headers
otel.propagators=tracecontext,baggage

# Estratégia de amostragem: coleta todos os traces (adequado para desenvolvimento)
otel.traces.sampler=parentbased_always_on

# Expõe os endpoints do Actuator necessários
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized

# Adiciona a tag "application" em todas as métricas para facilitar filtros no Prometheus
management.metrics.tags.application=${spring.application.name}
```

**Por que `parentbased_always_on`?** Em produção com alto volume, coletar 100% dos traces seria caro demais em armazenamento. Essa estratégia coleta todos os traces em desenvolvimento, mas respeita a decisão do serviço pai (upstream) quando a aplicação estiver atrás de um gateway — assim você pode controlar a taxa de amostragem externamente sem mudar código.

**Por que `tracecontext,baggage`?** Quando o frontend faz uma requisição para a API, o OTel injeta um identificador único (trace ID) no header HTTP. Se a API por sua vez chamar outro serviço, esse ID é propagado, criando um rastro único que atravessa todos os serviços. Sem isso, você veria fragmentos desconectados nos traces.

---

## Passo 3 — `ObservabilityConfig`: Métricas da JVM

**Arquivo:** `src/main/java/com/crescer_aprender/escala/config/ObservabilityConfig.java`

```java
@Configuration
public class ObservabilityConfig {
    @Bean public JvmMemoryMetrics jvmMemoryMetrics() { return new JvmMemoryMetrics(); }
    @Bean public JvmGcMetrics jvmGcMetrics()         { return new JvmGcMetrics(); }
    @Bean public JvmThreadMetrics jvmThreadMetrics() { return new JvmThreadMetrics(); }
    @Bean public ProcessorMetrics processorMetrics() { return new ProcessorMetrics(); }
}
```

**Por que métricas de JVM?** A JVM (Java Virtual Machine) é o ambiente de execução da aplicação. Saber o estado da JVM é essencial para diagnosticar problemas de desempenho:

- **`JvmMemoryMetrics`**: Monitora o uso de memória Heap e non-Heap. Se a Heap estiver no limite, a aplicação vai sofrer GC frequente e ficar lenta, ou até travar com `OutOfMemoryError`.
- **`JvmGcMetrics`**: Monitora o Garbage Collector (responsável por limpar memória não usada). GC muito frequente indica vazamento de memória ou alocação excessiva.
- **`JvmThreadMetrics`**: Monitora o número de threads ativas. Um pool de threads esgotado causa timeout nas requisições.
- **`ProcessorMetrics`**: Monitora o uso de CPU. Alta CPU pode indicar loop infinito ou algoritmo ineficiente.

Esses _binders_ se registram automaticamente no `MeterRegistry` do Spring (que é o ponto central de coleta de métricas), exportando para o Prometheus sem nenhuma configuração adicional.

---

## Passo 4 — Pacote `observability/`: Métricas de Negócio

Métricas de JVM dizem o que a _máquina_ está fazendo. Métricas de negócio dizem o que a _aplicação_ está fazendo no contexto do domínio: quantas escalas foram criadas? Quantos logins falharam?

Criamos três classes, uma para cada domínio da aplicação.

### 4.1 — `EscalaMetrics`

**Arquivo:** `src/main/java/com/crescer_aprender/escala/observability/EscalaMetrics.java`

Esta classe rastreia o ciclo de vida das escalas.

```java
@Component
public class EscalaMetrics {

    private final Counter criadas;
    private final Counter deletadas;
    private final Counter criacaoFalha;
    private final Timer   geracaoTimer;

    public EscalaMetrics(MeterRegistry registry) {
        // Counter: valor que só aumenta, nunca diminui
        criadas = Counter.builder("escala.criadas.total")
                .description("Total de escalas criadas com sucesso")
                .tag("dominio", "escala").register(registry);

        deletadas = Counter.builder("escala.deletadas.total")
                .description("Total de escalas deletadas")
                .tag("dominio", "escala").register(registry);

        criacaoFalha = Counter.builder("escala.criacao.falha.total")
                .description("Falhas na criação de escala")
                .tag("dominio", "escala").register(registry);

        // Timer: mede a duração de uma operação e publica percentis
        geracaoTimer = Timer.builder("escala.geracao.duracao")
                .description("Duração da geração automática de escala")
                .tag("dominio", "escala")
                .publishPercentiles(0.5, 0.95, 0.99)   // mediana, p95, p99
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(10))
                .maximumExpectedValue(Duration.ofSeconds(5))
                .register(registry);
    }
}
```

**Por que separar `criadas` de `criacaoFalha`?** Você precisa de ambos para calcular a _taxa de sucesso_ (`criadas / (criadas + criacaoFalha)`). Com apenas um counter você não conseguiria saber se zero escalas foram criadas porque ninguém tentou, ou porque todas as tentativas falharam.

**Por que um Timer com percentis?** A média é enganosa para latência. Se 99% das requisições levam 50ms, mas 1% leva 10 segundos, a média pode ser 150ms — um número que mascara um problema grave. Os percentis revelam a experiência real dos usuários:
- `p50` (mediana): metade das criações é mais rápida que esse valor.
- `p95`: 95% das criações são mais rápidas. Representa a "experiência típica do usuário lento".
- `p99`: os 1% piores casos. Importante para SLAs (acordos de nível de serviço).

**Por que a tag `"dominio": "escala"`?** Tags permitem filtrar e agrupar métricas no Prometheus. Com ela, você pode, por exemplo, ver todas as métricas do domínio "escala" com uma única query: `{dominio="escala"}`.

### 4.2 — `VoluntarioMetrics`

**Arquivo:** `src/main/java/com/crescer_aprender/escala/observability/VoluntarioMetrics.java`

```java
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

        // Gauge: valor que representa um estado atual (pode subir e descer)
        Gauge.builder("voluntario.ativos.total", ativos, AtomicInteger::get)
                .description("Voluntários ativos na plataforma")
                .tag("dominio", "voluntario").register(registry);
    }

    public void incrementCadastrados() { cadastrados.increment(); ativos.incrementAndGet(); }
    public void incrementDeletados()   { deletados.increment();   ativos.decrementAndGet(); }
    public void setAtivos(int total)   { ativos.set(total); }
}
```

**Por que um Gauge para voluntários ativos?** Um Gauge é diferente de um Counter: ele representa um _estado atual_ que pode tanto subir quanto cair. O número de voluntários ativos sobe quando alguém é cadastrado e cai quando alguém é removido. Um Counter jamais poderia representar isso, pois Counters nunca diminuem.

**Por que `AtomicInteger`?** A aplicação pode processar múltiplas requisições ao mesmo tempo (concorrência). `AtomicInteger` garante que as operações de incremento e decremento sejam atômicas (indivisíveis), evitando condições de corrida onde dois processos simultâneos corrompem o valor.

### 4.3 — `AuthMetrics`

**Arquivo:** `src/main/java/com/crescer_aprender/escala/observability/AuthMetrics.java`

```java
@Component
public class AuthMetrics {

    private final Counter loginSucesso;
    private final Counter loginFalha;
    private final Counter acessoNegado;

    public AuthMetrics(MeterRegistry registry) {
        loginSucesso = Counter.builder("auth.login.sucesso.total")
                .description("Logins bem-sucedidos")
                .tag("dominio", "seguranca").register(registry);

        loginFalha = Counter.builder("auth.login.falha.total")
                .description("Tentativas de login falhas")
                .tag("dominio", "seguranca").register(registry);

        acessoNegado = Counter.builder("auth.acesso.negado.total")
                .description("Requisições com 403 Forbidden")
                .tag("dominio", "seguranca").register(registry);
    }
}
```

**Por que monitorar falhas de login?** Um pico repentino de `auth.login.falha.total` é um sinal claro de ataque de força bruta ou de _credential stuffing_ (tentativas automatizadas com senhas vazadas). Com essa métrica você pode criar alertas no Prometheus para notificar a equipe de segurança quando isso acontecer.

**Por que separar `loginFalha` de `acessoNegado`?** São dois problemas completamente diferentes:
- `loginFalha`: o usuário não conseguiu _se autenticar_ (credenciais erradas) → erro 401 Unauthorized.
- `acessoNegado`: o usuário está autenticado, mas não tem _permissão_ para aquele recurso → erro 403 Forbidden. Pode indicar que um voluntário está tentando acessar funcionalidades administrativas.

---

## Passo 5 — Instrumentação dos Services e Controllers

Criar as classes de métricas não é suficiente — é preciso _chamar_ os métodos delas nos momentos certos dentro do código de negócio.

### 5.1 — `EscalaService.saveFromRequest`: Timer + Counters

**Arquivo:** `src/main/java/com/crescer_aprender/escala/service/EscalaService.java`

```java
@Transactional
public Escala saveFromRequest(EscalaCreateRequest request) {
    // 1. Inicia o cronômetro ANTES de qualquer operação
    Timer.Sample sample = escalaMetrics.iniciarTimer();
    try {
        // 2. Toda a lógica de negócio existente permanece intacta
        Escala escala = new Escala();
        escala.setMes(request.getMes());
        // ... resto da lógica ...

        Escala resultado = repository.save(escala);

        // 3. Sucesso: incrementa o counter de criações
        escalaMetrics.incrementCriadas();
        return resultado;
    } catch (Exception e) {
        // 4. Qualquer exceção: incrementa o counter de falhas e relança a exceção
        escalaMetrics.incrementCriacaoFalha();
        throw e;  // relança para que o handler de exceções da API trate normalmente
    } finally {
        // 5. O bloco finally é executado SEMPRE (sucesso ou falha), parando o timer
        escalaMetrics.finalizarTimer(sample);
    }
}
```

**Por que usar `try/catch/finally` aqui?** O bloco `finally` é a garantia de que o timer sempre será parado, independente de a operação ter tido sucesso ou lançado uma exceção. Sem ele, uma exceção inesperada deixaria o timer pendente e a métrica de duração teria valores incorretos.

**Por que relançar a exceção (`throw e`)?** Porque só queremos _observar_ o que acontece, não mudar o comportamento. A exceção precisa continuar seu fluxo normal até o handler que retorna o erro HTTP adequado ao cliente.

### 5.2 — `EscalaService.delete`: Counter de Deleção

```java
public boolean delete(Long id) {
    if (repository.existsById(id)) {
        repository.deleteById(id);
        escalaMetrics.incrementDeletadas();  // registra a deleção bem-sucedida
        return true;
    } else {
        throw new EntityNotFoundException("Escala", id);
    }
}
```

**Por que incrementar só depois do `deleteById`?** Se `deleteById` lançar uma exceção (por exemplo, problema de banco), o counter não seria incrementado — o que é o comportamento correto. Um counter de "deletadas" que inclui tentativas falhas seria um dado enganoso.

### 5.3 — `VoluntarioService`: Cadastro e Remoção

```java
// No método save():
Voluntario saved = repository.save(voluntario);
voluntarioMetrics.incrementCadastrados();  // atualiza counter E gauge de ativos
return saved;

// No método delete():
repository.deleteById(id);
voluntarioMetrics.incrementDeletados();    // atualiza counter E decrementa gauge de ativos
```

Note que `incrementCadastrados()` e `incrementDeletados()` atualizam tanto os counters (histórico acumulado) quanto o gauge de ativos (estado atual). Isso é fundamental: o gauge deve sempre refletir a realidade presente.

### 5.4 — `AuthController.login`: Métricas de Autenticação

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
    try {
        Authentication authentication = authenticationManager.authenticate(...);
        // ... gera token ...
        authMetrics.incrementLoginSucesso();   // contabiliza login bem-sucedido
        return ResponseEntity.ok(new AuthResponse(userData, token));
    } catch (BadCredentialsException e) {
        authMetrics.incrementLoginFalha();     // credenciais erradas → falha de login
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    } catch (Exception e) {
        // outros erros internos não são contabilizados como falha de login
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}
```

**Por que separar `BadCredentialsException` das outras exceções?** Não queremos contabilizar erros de infraestrutura (ex: banco fora do ar) como "falhas de login". Isso distorceria o alerta de segurança. Somente `BadCredentialsException` representa de fato uma tentativa de login com credenciais inválidas.

### 5.5 — `CustomAccessDeniedHandler`: Acesso Negado (403)

```java
@Override
public void handle(HttpServletRequest request, HttpServletResponse response,
                   AccessDeniedException accessDeniedException) throws IOException, ServletException {
    log.warn("Acesso negado para path={}", request.getRequestURI());
    authMetrics.incrementAcessoNegado();   // registra o 403
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    // ... escreve o JSON de resposta ...
}
```

Este handler é chamado pelo Spring Security sempre que um usuário autenticado tenta acessar um recurso para o qual não tem permissão. Ao incrementar `acessoNegado` aqui, capturamos _todos_ os 403 da aplicação em um único ponto, sem precisar instrumentar cada endpoint individualmente.

---

## Passo 6 — Infraestrutura Docker

Para visualizar os dados coletados localmente, precisamos subir os serviços de suporte.

### 6.1 — OTel Collector (`docker/otel-collector-config.yaml`)

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317   # Recebe dados da aplicação Spring Boot
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024         # Agrupa dados antes de enviar (reduz overhead de rede)
  memory_limiter:
    check_interval: 1s
    limit_mib: 256                # Evita que o Collector consuma memória indefinidamente

exporters:
  otlp/jaeger:
    endpoint: jaeger:14250        # Envia traces para o Jaeger
    tls:
      insecure: true              # Sem TLS em ambiente local
  prometheus:
    endpoint: "0.0.0.0:8889"     # Expõe métricas no formato Prometheus

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [otlp/jaeger, logging]
    metrics:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [prometheus, logging]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [logging]
```

**O que são pipelines?** Um pipeline no Collector é um caminho que os dados percorrem: `receiver` (entrada) → `processors` (transformações) → `exporters` (saída). Definimos um pipeline separado para traces, métricas e logs, pois cada um vai para um destino diferente.

**Por que o processor `batch`?** Sem ele, o Collector enviaria cada span/métrica individualmente para o destino, gerando muitas conexões de rede. O `batch` agrupa os dados e os envia em lotes, reduzindo significativamente o overhead.

### 6.2 — Prometheus (`docker/prometheus.yml`)

```yaml
global:
  scrape_interval: 15s   # A cada 15 segundos, Prometheus busca métricas dos targets

scrape_configs:
  - job_name: 'escala-api'
    static_configs:
      - targets: ['app:8080']         # A aplicação Spring Boot
    metrics_path: /actuator/prometheus # Endpoint onde as métricas estão disponíveis

  - job_name: 'otel-collector'
    static_configs:
      - targets: ['otel-collector:8889']  # O próprio Collector também expõe suas métricas
```

**Por que o Prometheus busca as métricas em vez de a aplicação enviá-las?** Esse modelo "pull" tem uma vantagem importante: se a aplicação cair, o Prometheus simplesmente não consegue fazer o scrape e registra o target como "down" — isso em si já é um alerta. No modelo "push", se a aplicação cair, nenhum dado chega ao servidor de métricas e você pode não perceber o problema.

### 6.3 — `docker-compose.yml`: Juntando Tudo

```yaml
services:
  db:         # PostgreSQL
  app:        # A aplicação Spring Boot (depende do db)
  otel-collector:  # OTel Collector (depende do jaeger)
  jaeger:          # Visualização de traces
  prometheus:      # Armazenamento de métricas

networks:
  escala-network:  # Rede interna que permite os containers se comunicarem pelo nome
    driver: bridge
```

**Por que uma rede dedicada (`escala-network`)?** Dentro do Docker Compose, cada serviço pode se comunicar com os outros pelo nome do serviço como hostname (ex: `otel-collector:4317`). A rede dedicada isola esses serviços do resto do sistema e garante que eles possam se descobrir pelo nome, sem precisar hardcodar IPs.

---

## Como Executar e Verificar

### 1. Subir toda a stack

```bash
docker compose up -d
```

### 2. Aguardar a aplicação iniciar (~30 segundos)

```bash
docker compose logs -f app
# Aguarde a mensagem: "Started EscalaApplication in X seconds"
```

### 3. Verificar os endpoints

| O quê                        | Onde acessar                                         |
|------------------------------|------------------------------------------------------|
| Health da aplicação          | http://localhost:8080/actuator/health                |
| Métricas no formato Prometheus | http://localhost:8080/actuator/prometheus           |
| Traces HTTP (Jaeger UI)      | http://localhost:16686                               |
| Consulta de métricas (Prometheus) | http://localhost:9090                           |

### 4. Exemplo de queries no Prometheus

```promql
# Taxa de criação de escalas por minuto (nos últimos 5 minutos)
rate(escala_criadas_total[5m])

# Percentil 95 de duração da geração de escala
histogram_quantile(0.95, rate(escala_geracao_duracao_bucket[5m]))

# Total de logins bem-sucedidos vs falhos
auth_login_sucesso_total
auth_login_falha_total

# Voluntários ativos no momento
voluntario_ativos_total

# Uso de memória Heap da JVM
jvm_memory_used_bytes{area="heap"}
```

### 5. Ver traces no Jaeger

1. Acesse http://localhost:16686
2. No campo "Service", selecione `escala-crescer-aprender-api`
3. Clique em "Find Traces"
4. Clique em qualquer trace para ver o detalhamento de cada operação (spans)
