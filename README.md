# pauta-votacao-api

API REST para gestão de pautas, sessões de votação, registro de votos e apuração de resultados.

## Objetivo
Disponibilizar um backend versionado e persistente para operações de votação, com regras de negócio claras e contrato HTTP padronizado.

## Escopo
- Apenas servidor (sem implementação da aplicação cliente).
- API REST versionada em URL: `/api/v1`.
- Persistência de pautas, sessões e votos.
- Regras principais:
  - Cada associado pode votar apenas uma vez por pauta.
  - Sessão de votação com duração informada ou 60 segundos por padrão.
  - Voto aceito apenas durante sessão aberta.
  - Resultado consolidado por pauta.

## API de negócio de votação
Base path: `/api/v1/pautas`

- `POST /api/v1/pautas`
  - Cria uma nova pauta.
- `POST /api/v1/pautas/{pautaId}/sessoes`
  - Abre sessão para uma pauta (`duracaoSegundos` opcional, default 60).
- `POST /api/v1/pautas/{pautaId}/votos`
  - Registra voto (`Sim` ou `Não`) para a pauta.
- `GET /api/v1/pautas/{pautaId}/resultado`
  - Retorna totais e resultado final da votação.

## Padrão de erro
Formato unificado para respostas de erro:

```json
{
  "timestamp": "2026-02-11T12:10:00Z",
  "status": 400,
  "message": "Sessão de votacao encerrada para esta pauta.",
  "path": "/api/v1/pautas/1/votos"
}
```

## Codigos HTTP previstos
- `200` consulta com sucesso
- `201` criação com sucesso
- `400` requisição inválida
- `404` recurso nao encontrado
- `409` conflito de regra de negócio (ex.: voto duplicado)
- `422` operação não permitida
- `500` erro interno
