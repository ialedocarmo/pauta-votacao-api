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

## Escolhas tecnicas (resumo)
- Spring Boot + Spring Data JPA + Flyway para simplicidade e manutencao.
- PostgreSQL para persistencia relacional com constraints de integridade.
- `Clock` injetavel para regras temporais testaveis.
- Erros padronizados no backend para facilitar consumo por clientes.

## API de negocio de votacao
Base path: `/api/v1/pautas`

- `POST /api/v1/pautas`
  - Cria uma nova pauta.
- `POST /api/v1/pautas/{pautaId}/sessoes`
  - Abre sessao para uma pauta (`duracaoSegundos` opcional, default 60).
- `POST /api/v1/pautas/{pautaId}/votos`
  - Registra voto (`SIM` ou `NAO`) para a pauta.
- `GET /api/v1/pautas/{pautaId}/resultado`
  - Retorna totais e resultado final da votacao.

## Padrao de erro
Formato unificado para respostas de erro:

```json
{
  "timestamp": "2026-02-11T12:10:00Z",
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

## Execucao local
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

## Como testar (PowerShell)
1. Criar uma pauta:

```powershell
$pautaA = Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas" `
  -ContentType "application/json" `
  -Body '{"titulo":"Reforma do Estatuto"}'
```

2. Abrir sessao com duracao padrao (60 segundos):

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas/$($pautaA.id)/sessoes" `
  -ContentType "application/json" `
  -Body '{}'
```

3. Registrar voto:

```powershell
Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/v1/pautas/$($pautaA.id)/votos" `
  -ContentType "application/json" `
  -Body '{"associadoId":"assoc-001","voto":"SIM"}'
```

4. Consultar resultado:

```powershell
Invoke-RestMethod -Method GET `
  -Uri "http://localhost:8080/api/v1/pautas/$($pautaA.id)/resultado"
```

## Logs
A aplicacao registra eventos de negocio relevantes em nivel `INFO`:
- criacao de pauta
- abertura de sessao
- registro de voto
- consulta de resultado

Configuracao atual em `application.properties`:
- `logging.level.root=INFO`
- `logging.level.com.ialedocarmo.pauta_votacao_api=INFO`

## Testes automatizados
Executar:

```bash
mvn test
```

Cobertura atual de regras criticas:
- duracao padrao de sessao
- bloqueio de voto com sessao encerrada
- normalizacao de `associadoId` antes de persistir

## Qualidade e limpeza
- Flyway para versionamento de schema (`V1`, `V2`, `V3`).
- Constraints no banco para integridade:
  - unicidade de sessao por pauta
  - unicidade de voto por pauta + associado
  - check para valores de voto validos
- Tratamento centralizado de excecoes (`GlobalExceptionHandler`).

## Estrategia de commits
Commits curtos, semanticos e incrementais, por exemplo:
- `feat: implementar registro de votos com regra de voto unico por associado`
- `feat: implementar apuracao e consulta de resultado por pauta`
- `feat: padronizar tratamento global de erros e logs`
- `test: adicionar testes unitarios para regras de sessao e voto`
- `docs: atualizar readme com arquitetura, testes e decisoes tecnicas`
