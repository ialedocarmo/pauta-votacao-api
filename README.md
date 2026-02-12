# pauta-votacao-api

API REST para gestao de pautas, sessoes de votacao, registro de votos e apuracao de resultados.

## Objetivo
Disponibilizar um backend versionado e persistente para operacoes de votacao, com regras de negocio claras e contrato HTTP padronizado.

## Escopo
- Apenas servidor (sem implementacao da aplicacao cliente).
- API REST versionada em URL: `/api/v1`.
- Persistencia de pautas, sessoes e votos.
- Regras principais:
  - Cada associado pode votar apenas uma vez por pauta.
  - Sessao de votacao com duracao informada ou 60 segundos por padrao.
  - Voto aceito apenas durante sessao aberta.
  - Resultado consolidado por pauta.

## Arquitetura e organizacao
- Estrutura por dominio: `pauta`, `sessao`, `voto`.
- Camadas por dominio:
  - `api`: controllers e DTOs
  - `service`: regras de negocio
  - `repository`: acesso a dados
  - `domain`: entidades e enums
- Cross-cutting em `common` e `config`:
  - `GlobalExceptionHandler`
  - `ApiError`
  - `TimeConfig` com `Clock` injetavel

## Escolhas tecnicas
- Spring Boot + Spring Data JPA + Flyway para simplicidade e manutencao.
- PostgreSQL para persistencia relacional com constraints de integridade.
- `Clock` injetavel para regras temporais testaveis.
- Erros padronizados no backend para facilitar consumo por clientes.

## Qualidade e limpeza
- Flyway para versionamento de schema (`V1`, `V2`, `V3`, `V4`).
- Constraints e indices no banco para integridade/performance:
  - unicidade de sessao por pauta
  - unicidade de voto por pauta + associado
  - check para valores de voto validos
  - indice composto para apuracao: `(pauta_id, voto)`
- Tratamento centralizado de excecoes (`GlobalExceptionHandler`).

## Execucao local

### Requisitos
- Java 25+
- Maven 3.9+
- Docker + Docker Compose

### Comandos
1. Subir o banco:
```bash
docker compose up -d
```
2. Rodar a API:
```bash
mvn spring-boot:run
```
3. Parar e remover volumes (reset de dados local):
```bash
docker compose down -v
```

## Swagger / OpenAPI
- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

## Endpoints e exemplos JSON
Base path: `/api/v1/pautas`

### 1) Criar pauta
`POST /api/v1/pautas`

Request:
```json
{
  "titulo": "Reforma do Estatuto"
}
```

Response `201`:
```json
{
  "id": 1,
  "titulo": "Reforma do Estatuto",
  "createdAt": "2026-02-12T12:00:00Z"
}
```

### 2) Abrir sessao
`POST /api/v1/pautas/{pautaId}/sessoes`

Request (duracao explicita):
```json
{
  "duracaoSegundos": 120
}
```

Request (padrao 60s):
```json
{}
```

Response `201`:
```json
{
  "id": 10,
  "pautaId": 1,
  "inicio": "2026-02-12T12:05:00Z",
  "fim": "2026-02-12T12:07:00Z",
  "duracaoSegundos": 120
}
```

### 3) Registrar voto
`POST /api/v1/pautas/{pautaId}/votos`

Request:
```json
{
  "associadoId": "assoc-001",
  "voto": "SIM"
}
```

Response `201`:
```json
{
  "id": 100,
  "pautaId": 1,
  "associadoId": "assoc-001",
  "voto": "SIM",
  "createdAt": "2026-02-12T12:06:00Z"
}
```

### 4) Consultar resultado
`GET /api/v1/pautas/{pautaId}/resultado`

Response `200`:
```json
{
  "pautaId": 1,
  "titulo": "Reforma do Estatuto",
  "totalSim": 10,
  "totalNao": 7,
  "totalVotos": 17,
  "resultado": "APROVADA"
}
```

## Padrao de erro
Formato unificado para respostas de erro:
```json
{
  "timestamp": "2026-02-12T12:10:00Z",
  "status": 400,
  "message": "sessao de votacao encerrada para esta pauta",
  "path": "/api/v1/pautas/1/votos"
}
```

## Codigos HTTP previstos
- `200` consulta com sucesso
- `201` criacao com sucesso
- `400` requisicao invalida
- `404` recurso nao encontrado
- `409` conflito de regra de negocio (ex.: voto duplicado)
- `500` erro interno

## Testes automatizados
### 1) Executar testes unitarios:
```bash
mvn test
```

Cobertura atual de regras criticas:
- duracao padrao de sessao
- bloqueio de voto com sessao encerrada
- normalizacao de `associadoId` antes de persistir


### 2) Gerar cobertura com JaCoCo:
```bash
mvn verify
```

Relatorio de cobertura:
- `target/site/jacoco/index.html`

### 3) Teste de performance
Teste de carga com k6 (via Docker, sem instalacao local):

```powershell
docker run --rm -i `
  -v "${PWD}:/work" `
  -w /work `
  -e BASE_URL=http://host.docker.internal:8080 `
  grafana/k6 run perf/k6-votacao.js
```

Criterios de sucesso esperados:
- `http_req_failed < 1%`
- `http_req_duration p(95) < 800ms`

Observacao: antes do teste, suba banco (`docker compose up -d`) e API (`mvn spring-boot:run`).

## URLs dinamicas
Para evitar dominio hardcoded em links/callbacks, a aplicacao usa a propriedade:
```properties
app.urls.callback-base-url=http://localhost:8080
```

Essa propriedade e usada pelo `ApiUrlBuilder` (`src/main/java/com/ialedocarmo/pauta_votacao_api/common/url/ApiUrlBuilder.java`) para montar URLs absolutas.
Os endpoints `POST` retornam `Location` com essa base configuravel.

Exemplos por ambiente:
- local desktop: `http://localhost:8080`
- emulador/dispositivo na mesma rede: `http://192.168.0.10:8080`
- ambiente remoto: `https://api.seudominio.com`
