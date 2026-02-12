# pauta-votacao-api

API REST para gestão de pautas, sessães de votação, registro de votos e apuração de resultados.

## Objetivo
Disponibilizar um backend versionado e persistente para operações de votação, com regras de negócio claras e contrato HTTP padronizado.

## Escopo
- Apenas servidor (sem implementação da aplicação cliente).
- API REST versionada em URL: `/api/v1`.
- Persistencia de pautas, sessões e votos.
- Regras principais:
  - Cada associado pode votar apenas uma vez por pauta.
  - Sessão de votação com duração informada ou 60 segundos por padrão.
  - Voto aceito apenas durante sessão aberta.
  - Resultado consolidado por pauta.

## API de negocio de votacao
Base path: `/api/v1/pautas`

- `POST /api/v1/pautas`
  - Cria uma nova pauta.
- `POST /api/v1/pautas/{pautaId}/sessoes`
  - Abre sessão para uma pauta (`duracaoSegundos` opcional, default 60).
- `POST /api/v1/pautas/{pautaId}/votos`
  - Registra voto (`SIM` ou `NAO`) para a pauta.
- `GET /api/v1/pautas/{pautaId}/resultado`
  - Retorna totais e resultado final da votação.

## Padrão de erro
Formato unificado para respostas de erro:

```json
{
  "timestamp": "2026-02-11T12:10:00Z",
  "status": 400,
  "message": "Sessao de votacao encerrada para esta pauta.",
  "path": "/api/v1/pautas/1/votos"
}
```

## Códigos HTTP previstos
- `200` consulta com sucesso
- `201` criação com sucesso
- `400` requisição inválida
- `404` recurso não encontrado
- `409` conflito de regra de negócio (ex.: voto duplicado)
- `422` operação não permitida
- `500` erro interno

## Execução local
1. Subir o banco PostgreSQL:

```bash
docker compose up -d
```

2. Rodar a API:

```bash
mvn spring-boot:run
```

3. Parar containers:

```bash
docker compose down
```

4. Parar e remover volumes (reset de dados local):

```bash
docker compose down -v
```

## Como testar
1. Criar uma pauta (PowerShell - recomendado no Windows):

```powershell
$pauta = Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas" `
  -ContentType "application/json" `
  -Body '{"titulo":"Reforma do Estatuto"}'
```

Resposta esperada:
- HTTP `201 Created`
- JSON com `id`, `titulo` e `createdAt`

2. Testar validação de entrada (título vazio):

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas" `
  -ContentType "application/json" `
  -Body '{"titulo":""}'
```

Resposta esperada:
- HTTP `400 Bad Request`
- JSON no padrão de erro com `timestamp`, `status`, `message` e `path`

3. Abrir sessão com duração padrão (60 segundos):

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas/$($pauta.id)/sessoes" `
  -ContentType "application/json" `
  -Body '{}'
```

Resposta esperada:
- HTTP `201 Created`
- JSON com `id`, `pautaId`, `inicio`, `fim` e `duracaoSegundos=60`

4. Abrir sessão com duração explicita:

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas/$($pauta.id)/sessoes" `
  -ContentType "application/json" `
  -Body '{"duracaoSegundos":120}'
```

Resposta esperada:
- HTTP `201 Created`
- JSON com `duracaoSegundos=120`

5. Erros esperados da abertura de sessão:
- `404 Not Found` quando `pautaId` não existe.
- `409 Conflict` quando a pauta já possui sessão cadastrada.
