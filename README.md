# Escala API

**Escala API** é uma API REST desenvolvida em Java com Spring Boot para facilitar o cadastro e o controle de escalas de voluntários do projeto social **Crescer e Aprender**. Esse projeto tem como missão oferecer aulas gratuitas de **Informática**, **Matemática** e **Língua Portuguesa** para crianças em situação de vulnerabilidade social.

---

## 💡 Propósito

Todos os sábados, o projeto realiza aulas presenciais com as crianças, e para garantir o bom andamento dessas atividades, é necessário organizar a participação dos voluntários de forma equilibrada e eficiente. Com um grupo de aproximadamente **40 voluntários ativos**, é essencial distribuir a escala de maneira que:

- Cada aula tenha **no mínimo 4** e **no máximo 8 voluntários**.
- A disponibilidade dos voluntários seja respeitada.
- A escala do mês seja gerada com antecedência, de forma justa e transparente.

A API automatiza esse processo de organização, permitindo:

- Cadastro de voluntários e suas disponibilidades.
- Definição das datas de aula por mês.
- Geração automática de escalas conforme regras definidas.
- Visualização de escalas por data ou por voluntário.

---

## 🛠️ Tecnologias e Stacks

- **Java 21**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **Flyway** para migrações de banco de dados
- **PostgreSQL**
- **Lombok**
- **Swagger (OpenAPI 3)** para documentação de endpoints
- **JUnit 5** para testes unitários
- **Docker Compose** para subir aplicação e banco de dados de uma vez

---

## ✅ Funcionalidades

- [x] **Cadastro de Voluntários**
- [x] **Registro de disponibilidade** dos voluntários
- [x] **Cadastro de datas de aulas**
- [x] **Geração automática da escala**
- [x] **Visualização da escala por data**
- [x] **Visualização da escala por voluntário**
- [x] **Edição manual da escala**
- [x] **Autenticação e autorização para administradores/coordenação**

> Observação: A funcionalidade de autenticação/autorizações foi implementada via JWT. Endpoints protegidos exigem o cabeçalho `Authorization: Bearer <token>` e controles de acesso por role (por exemplo, `COORDENADOR`).

---

## ▶️ Como Rodar o projeto
Na pasta raiz do projeto, execute:
```bash
docker compose up --build
```
ou, localmente sem Docker:
```bash
./mvnw -DskipTests package
java -jar target/escala-0.0.1-SNAPSHOT.jar
```

---

## 📚 Endpoints da API

### 🔹 Autenticação
- `POST /auth/login` — Faz login e retorna um token JWT (string no corpo). Exemplo de request body:
```json
{ "username": "email@exemplo.com", "password": "senha" }
```
Resposta: token JWT em texto puro (200) ou 401 em caso de credenciais inválidas.

### 🔹 Voluntários
- `GET /crescer-aprender/voluntarios` — Lista todos os voluntários
- `GET /crescer-aprender/voluntarios/{id}` — Retorna voluntário por ID
- `POST /crescer-aprender/voluntarios` — Cria um novo voluntário (requer role `COORDENADOR`)
- `PUT /crescer-aprender/voluntarios/{id}` — Atualiza um voluntário
- `DELETE /crescer-aprender/voluntarios/{id}` — Remove um voluntário (não pode estar escalado; requer `COORDENADOR`)

### 🔹 Escalas
- `GET /crescer-aprender/escala` — Lista todas as escalas
- `GET /crescer-aprender/escala/byId/{id}` — Retorna a escala por ID
- `GET /crescer-aprender/escala/byDate/{data}` — Busca escala por data (formato: `yyyy-MM-dd`)
- `GET /crescer-aprender/escala/buscar-por-mes-ano-voluntario?mes={mes}&ano={ano}&idVoluntario={id}` — Busca escala por mês, ano e voluntário
- `POST /crescer-aprender/escala` — Gera uma nova escala (requer `COORDENADOR`)
- `PUT /crescer-aprender/escala/{id}` — Edita uma escala existente (requer `COORDENADOR`)
- `DELETE /crescer-aprender/escala/{id}` — Deleta uma escala por ID (requer `COORDENADOR`)

---

## 🛡️ Segurança e uso do Swagger

A documentação interativa do Swagger está disponível em:
```
http://localhost:8080/swagger-ui/index.html
```

No Swagger UI use o botão **Authorize** e cole o token no formato:
```
Bearer <TOKEN>
```
Depois de autorizado, as operações protegidas aceitarão o cabeçalho automaticamente.

---

## ⚠️ Tratamento de Erros

A aplicação utiliza exceções customizadas para fornecer mensagens claras e específicas. Além disso, respostas de segurança (401/403) são padronizadas em JSON com o schema `ErrorResponse`.

Exceções customizadas existentes (respostas e quando são lançadas):

- `EntityNotFoundException` – quando uma entidade não é localizada.
- `EmailAlreadyExistsException` – e-mail de voluntário já está em uso.
- `VoluntarioIsScheduledException` – impede exclusão de voluntário escalado.
- `EscalaAlreadyExistsException` – evita duplicidade de escalas por mês/ano.
- `InvalidVoluntarioDataException` – dados inválidos no cadastro.
- `DatabaseException` – falha ao acessar o banco de dados.

Erros de autenticação/autorização (padronizados):
- 401 Unauthorized — quando o token está ausente ou inválido (handled by `CustomAuthenticationEntryPoint`).
- 403 Forbidden — quando o usuário está autenticado, mas não tem permissão para o recurso (handled by `CustomAccessDeniedHandler`).

Exemplo de `ErrorResponse` (JSON):
```json
{
  "timestamp": "2025-10-25T06:46:38.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Você não tem permissão para acessar este recurso",
  "path": "/crescer-aprender/voluntarios"
}
```

---

## 🔎 Documentação Swagger

A API possui uma documentação interativa disponível via Swagger UI, acessível em:

```
http://localhost:8080/swagger-ui/index.html
```

Por essa interface você pode:

- Consultar todos os endpoints disponíveis
- Visualizar exemplos de requisições e respostas
- Testar chamadas diretamente pelo navegador (lembre-se de autorizar com o token JWT)

```
o usuario padrão é: 
admin@email.com 
e a senha é:
cresceraprender
```
-   *essa senha deve ser deletada após criados os primeiros usuários administradores*
---

## 📌 Sobre o Projeto Crescer e Aprender

O **Crescer e Aprender** é um projeto social voltado para a educação de crianças em situação de vulnerabilidade, proporcionando um ambiente de acolhimento e desenvolvimento por meio da educação e do voluntariado. Acreditamos que a **educação é a base para transformação social**.

---

## 🤝 Contribuições

Este projeto é **open source** e colaborações são muito bem-vindas! Para contribuir:

1. Faça um fork do projeto.
2. Crie sua branch: `git checkout -b minha-contribuicao`
3. Envie um pull request para revisão.

---

## 📝 Possíveis Melhorias Futuras

- Adicionar filtros de busca por nome e e-mail de voluntário
- Criar testes de integração que validem fluxos de autenticação e autorização

---

## 📞 Contato

- Desenvolvedor: Luiz Henrique
- GitHub: [luizhenrique3651/escala-crescer-aprender-api](https://github.com/luizhenrique3651/escala-crescer-aprender-api)
- Instagram do projeto: [proj_crescereaprender](https://www.instagram.com/proj_crescereaprender/)
