# Escala API (Projeto Crescer e Aprender)

**Escala API** é uma solução robusta desenvolvida em **Java 21** e **Spring Boot 3.5.10** para a orquestração e gestão de voluntários do projeto social **Crescer e Aprender**. O projeto visa automatizar a distribuição de voluntários em aulas semanais, garantindo o cumprimento de regras sociais e operacionais críticas.

---

## 💡 Missão e Regras de Negócio
O projeto social atende crianças em situação de vulnerabilidade com aulas de Informática, Matemática e Português. Para manter a qualidade pedagógica e segurança, a API impõe as seguintes regras:

- **Regra de Ouro**: As aulas e escalas ocorrem **estritamente aos sábados**.
- **Capacidade Controlada**: Cada aula deve ter no mínimo **4** e no máximo **8** voluntários.
- **Justiça na Escala**: O sistema prioriza voluntários com menor número de alocações no período.
- **Imutabilidade**: Voluntários com escalas futuras não podem ser removidos do sistema.

---

## 🛠️ Stack Tecnológica de Alta Performance
- **Linguagem**: Java 21 (Uso intensivo de Records e Pattern Matching).
- **Framework**: Spring Boot 3.5.10.
- **Persistência**: Spring Data JPA com PostgreSQL.
- **Migrações**: Flyway (Versionamento de schema).
- **Segurança**: Spring Security + JWT (Stateless) com Roles (`COORDENADOR`, `VOLUNTARIO`).
- **Qualidade**: SonarQube (Análise Estática) e JaCoCo (Cobertura de Código).
- **Documentação**: Swagger/OpenAPI 3.

---

## 📊 Qualidade e Observabilidade
O projeto possui uma infraestrutura de qualidade integrada via Docker:

- **SonarQube**: Dashboard de qualidade em `localhost:9000` (Persistido em PostgreSQL).
- **JaCoCo**: Meta de cobertura de código > 80%.
- **Automação**: Script `./run-analysis.sh` na raiz para disparo de ciclo completo (Build -> Test -> Coverage -> Sonar).

---

## 🚀 Como Executar o Ecossistema

### 1. Ambiente Docker (Completo)
Sobe a API, o Banco de Dados e o Servidor de Qualidade:
```bash
docker compose up --build -d
```

### 2. Disparar Análise de Qualidade
Para rodar todos os testes unitários e enviar o relatório ao SonarQube:
```bash
./run-analysis.sh
```

### 3. Desenvolvimento Local (Maven)
```bash
./mvnw spring-boot:run
```

---

## ✅ Funcionalidades Principais

- [x] **Geração Balanceada**: Motor que gera sugestões de escala baseadas em disponibilidade e frequência.
- [x] **@IsSaturday**: Validação customizada via Bean Validation para datas.
- [x] **CapacityGuard**: Componente de segurança para limites de voluntários.
- [x] **Segurança JWT**: Endpoints de coordenação protegidos por roles.
- [x] **CRUD Completo**: Gestão de Voluntários, Escalas e Usuários.

---

## 📚 Endpoints Estratégicos

### Geração de Sugestão
- `POST /crescer-aprender/escala/gerar-sugestao` — Gera uma proposta de escala balanceada para uma lista de datas.

### Autenticação
- `POST /auth/login` — Autentica e retorna o perfil detalhado do voluntário.

### Gestão de Escalas
- `GET /crescer-aprender/escala/byDate/{data}` — Busca escalas para um sábado específico.
- `PUT /crescer-aprender/escala/popula-voluntarios/{id}` — Popula automaticamente uma escala vazia.

---

## 🛡️ Segurança e Documentação
A documentação interativa está disponível em:
`http://localhost:8080/swagger-ui/index.html`

**Usuário Padrão**: `admin@email.com` / `cresceraprender`

---

## 📌 Sobre o Crescer e Aprender
Um projeto social que acredita na **educação como base para transformação social**. Este software é uma ferramenta para potencializar o impacto dessa missão.

---

## 📞 Contato e Contribuição
- **Desenvolvedor**: Luiz Henrique
- **GitHub**: [luizhenrique3651](https://github.com/luizhenrique3651)
- **Instagram**: [@proj_crescereaprender](https://www.instagram.com/proj_crescereaprender/)
