# auth-service

Microsserviço de **autenticação JWT do professor** — parte da decomposição do projeto
**fábrica-de-software** em microsserviços. Responsável por: cadastro de credencial,
login com emissão de JWT e validação de token por introspecção.

## Stack
Spring Boot 4.0.5 · Java 17 · PostgreSQL · Spring Security (BCrypt) · JJWT (HS256).

## Como rodar
```bash
docker compose up -d        # sobe o PostgreSQL (auth_service_db)
./mvnw spring-boot:run      # sobe o serviço na porta 8081
```
Configuração por variáveis de ambiente (com defaults para dev):
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `AUTH_JWT_SECRET`, `AUTH_JWT_EXPIRATION`.

> O segredo do JWT (`AUTH_JWT_SECRET`) deve ter **no mínimo 32 bytes** (HS256) e ser
> definido por configuração — nunca fica aleatório por boot nem hardcoded.

## Endpoints
| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Cria credencial (professorId, email, senha) |
| POST | `/auth/login` | Valida credenciais e retorna JWT |
| POST | `/auth/validate` | Introspecção: valida token e devolve as claims |

A validação de token por outros serviços (gateway, project-service etc.) é feita via
`POST /auth/validate` — assim o segredo do JWT permanece interno ao auth-service.
Token inválido/expirado retorna `200` com `valid:false` (não é erro HTTP).

### Exemplos
```bash
# Cadastrar credencial
curl -X POST localhost:8081/auth/register -H "Content-Type: application/json" \
  -d '{"professorId":1,"email":"ana@escola.com","senha":"segredo123"}'

# Login
curl -X POST localhost:8081/auth/login -H "Content-Type: application/json" \
  -d '{"email":"ana@escola.com","senha":"segredo123"}'

# Validar token
curl -X POST localhost:8081/auth/validate -H "Content-Type: application/json" \
  -d '{"token":"<TOKEN>"}'
```

## Testes
```bash
./mvnw test
```

## Documentação de design
- Spec: [`docs/specs/2026-06-05-auth-service-design.md`](docs/specs/2026-06-05-auth-service-design.md)
