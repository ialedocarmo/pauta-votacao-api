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
  "message": "Sessao de votacao encerrada para esta pauta.",
  "path": "/api/v1/pautas/1/votos"
}
```

## Codigos HTTP previstos
- `200` consulta com sucesso
- `201` criacao com sucesso
- `400` requisicao invalida
- `404` recurso nao encontrado
- `409` conflito de regra de negocio (ex.: voto duplicado)
- `422` operacao nao permitida
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