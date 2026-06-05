# auth-service — Design (login e autenticação JWT do professor)

**Data:** 2026-06-05
**Autor:** Fernanda Fires
**Status:** Aprovado

## Contexto

O projeto **fábrica-de-software** (monolito Spring Boot em
`https://github.com/JulioMarinho5/fabrica-de-software.git`) está sendo decomposto
em microserviços, divididos por pessoa:

| Serviço | Responsável | Responsabilidade |
|---|---|---|
| API Gateway & Service Discovery | Julio | Roteamento e descoberta de serviços |
| academic-service | Erica | Professor, Aluno, Grupo |
| **auth-service** | **Fernanda** | **Login e autenticação JWT do professor** |
| notification-service | Jonathan | Notificação de email para o professor |
| project-service | José | Projetos solicitados pelo professor e validados pelo admin |

Este documento cobre **apenas o auth-service**.

## Objetivo e escopo

**Objetivo:** um microserviço independente que autentica o professor (valida
credenciais), emite tokens JWT e valida tokens via introspecção.

**No escopo:**
- Cadastro de credencial do professor (`POST /auth/register`).
- Login e emissão de JWT (`POST /auth/login`).
- Validação de token por introspecção (`POST /auth/validate`).

**Fora do escopo (YAGNI / pertence a outros serviços):**
- Cadastro de professor/aluno/grupo (academic-service).
- Envio de email / RabbitMQ (notification-service).
- Projetos (project-service).
- Roteamento e descoberta de serviços (API Gateway & Service Discovery).
- Refresh token, recuperação de senha, roles além de `PROFESSOR`.

## Decisões de arquitetura

| Tema | Decisão |
|---|---|
| Stack | Spring Boot 4.0.5, Java 17 (mesma do monolito) |
| Porta | 8081 |
| Credenciais | Banco **próprio** (`auth_service_db`), tabela `credenciais`. Sem banco compartilhado. |
| `professorId` | Dado externo (vem do academic-service), guardado na credencial só para ir no token |
| Assinatura JWT | HS256, segredo **configurável** (`auth.jwt.secret` via env). Segredo fica **interno** ao auth-service |
| Validação por outros serviços | **Introspecção**: chamam `POST /auth/validate`. Não precisam do segredo |
| Provisionamento de credencial | `POST /auth/register` no próprio auth-service. Coordenação com academic-service fica a cargo de quem chama |
| Hash de senha | BCrypt |
| Estrutura | Projeto Maven standalone novo, pacote `com.fabricadesoftware.authservice` |

## Arquitetura geral

```
auth-service (Spring Boot 4.0.5 / Java 17, porta 8081)
│
├── PostgreSQL próprio (auth_service_db, tabela: credenciais)
│
├── API REST
│   ├── POST /auth/register   → cria credencial (email, senha, professorId)
│   ├── POST /auth/login      → valida credenciais e emite JWT (HS256)
│   └── POST /auth/validate   → introspecção: valida token e devolve claims
│
└── JWT interno (segredo configurável — corrige bug do segredo aleatório)
```

**Fluxo:**
1. academic-service (ou teste) chama `POST /auth/register` → grava credencial com senha em BCrypt.
2. Professor chama `POST /auth/login` → valida hash → retorna JWT + dados básicos.
3. Gateway/serviços chamam `POST /auth/validate` → recebem `{valid, professorId, email, role, expiraEm}`.

## Modelo de dados

**Tabela `credenciais`:**

| Coluna | Tipo | Observação |
|---|---|---|
| `id` | BIGINT (PK, identity) | id interno do auth-service |
| `professor_id` | BIGINT, unique, not null | id do professor no academic-service |
| `email` | VARCHAR(150), unique, not null | armazenado em lowercase |
| `senha` | VARCHAR(255), not null | hash BCrypt |
| `role` | VARCHAR(20), not null | `PROFESSOR` (default) |
| `data_criacao` | TIMESTAMP, not null | auditoria |

## Contratos dos endpoints

### `POST /auth/register`
```jsonc
// req
{ "professorId": 1, "email": "ana@escola.com", "senha": "segredo123" }
// 201 → { "mensagem": "Credencial criada com sucesso", "timestamp": "..." }
// 409 → email ou professorId já cadastrado
// 400 → validação (email inválido, senha curta, campos faltando)
```

### `POST /auth/login`
```jsonc
// req
{ "email": "ana@escola.com", "senha": "segredo123" }
// 200 → { "token": "eyJ...", "professorId": 1, "email": "ana@escola.com",
//         "role": "PROFESSOR", "expiraEm": "..." }
// 401 → senha incorreta
// 404 → email não encontrado
```

### `POST /auth/validate` (introspecção)
```jsonc
// req
{ "token": "eyJ..." }   // aceita também header Authorization: Bearer <token>
// 200 → { "valid": true, "professorId": 1, "email": "ana@escola.com",
//         "role": "PROFESSOR", "expiraEm": "..." }
// 200 → { "valid": false, "motivo": "Token expirado" }
```
Token inválido **não** é erro HTTP — é resposta de introspecção (200 com `valid:false`).

### JWT
- Algoritmo HS256.
- Segredo via `auth.jwt.secret` (env `AUTH_JWT_SECRET`).
- Expiração via `auth.jwt.expiration` (default 1h / 3600000 ms).
- Claims: `sub` = email, `professorId`, `role`, `iat`, `exp`.

### Tratamento de erros
`@RestControllerAdvice` devolve JSON padronizado `{ erro, mensagem, timestamp }`
com o status adequado (400/401/404/409).

## Estrutura do projeto

Pacote base: `com.fabricadesoftware.authservice`

```
auth-service-repo/
├── pom.xml                          # Spring Boot 4.0.5, Java 17 — só deps necessárias
├── README.md                        # como rodar, endpoints, exemplos curl
├── docker-compose.yml               # PostgreSQL local (opcional)
└── src/
    ├── main/java/com/fabricadesoftware/authservice/
    │   ├── AuthServiceApplication.java
    │   ├── config/SecurityConfig.java          # BCrypt, stateless, CORS, libera /auth/**
    │   ├── controllers/AuthController.java
    │   ├── services/
    │   │   ├── CredencialService.java          # register + login
    │   │   └── JwtService.java                 # gerar/validar token (segredo configurável)
    │   ├── entities/Credencial.java
    │   ├── repositories/CredencialRepository.java
    │   ├── dtos/                                # Register/Login/Token/Validate/Mensagem
    │   ├── exceptions/                          # EmailJaCadastrado, CredencialNaoEncontrada, SenhaIncorreta
    │   └── advice/GlobalExceptionHandler.java
    └── test/java/com/fabricadesoftware/authservice/
        ├── services/JwtServiceTest.java
        ├── services/CredencialServiceTest.java
        └── controllers/AuthControllerTest.java
```

**Dependências:** spring-boot-starter-web, -data-jpa, -security, -validation,
postgresql, jjwt (api/impl/jackson), lombok, e para teste: spring-boot-starter-test,
spring-security-test, H2.
**Sem** mail, rabbitmq, thymeleaf.

## Configuração

`application.properties` com leitura de env vars, sem segredos commitados:

```properties
server.port=8081
spring.application.name=auth-service
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/auth_service_db}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
auth.jwt.secret=${AUTH_JWT_SECRET:troque-este-segredo-em-producao-min-32-bytes}
auth.jwt.expiration=${AUTH_JWT_EXPIRATION:3600000}
```

## Estratégia de testes (TDD)

Testes escritos antes da implementação. Banco H2 em memória para service/repository.

- **JwtServiceTest:** gera→valida (round-trip), token expirado, token malformado/assinatura inválida.
- **CredencialServiceTest:** register feliz, register com email/professorId duplicado,
  login ok, login com senha errada, login com email inexistente.
- **AuthControllerTest** (`@WebMvcTest`): os 3 endpoints — status e corpo de resposta,
  incluindo `validate` retornando `valid:false` para token inválido.

## Critérios de aceite

1. `mvn test` passa.
2. `POST /auth/register` cria credencial e rejeita duplicados (409).
3. `POST /auth/login` retorna JWT válido para credenciais corretas; 401/404 nos casos de erro.
4. `POST /auth/validate` valida um token emitido pelo login e responde `valid:false`
   (200) para token expirado/malformado.
5. Segredo JWT vem de configuração (não é aleatório por boot, não está hardcoded).
6. O serviço sobe isolado, sem dependência de mail/rabbitmq/academic-service.
