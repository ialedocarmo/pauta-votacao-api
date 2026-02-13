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

## Decisoes de implementacao
- Arquitetura em camadas por dominio (`pauta`, `sessao`, `voto`) para manter separacao clara entre API, regra de negocio e persistencia, facilitando manutencao e testes.
- Versionamento da API em `/api/v1` para permitir evolucao sem quebra de contrato.
- Flyway para versionamento de banco e reprodutibilidade do ambiente.
- PostgreSQL com constraints de unicidade e integridade para reforcar regras de negocio tambem no banco.
- `Clock` injetavel para regras temporais previsiveis e testaveis (sessao e timestamps).
- Tratamento centralizado de erros via `GlobalExceptionHandler` com payload padronizado (`timestamp`, `status`, `message`, `path`).
- `CreatedResponseFactory` para padronizar respostas `201 Created` com header `Location`.
- Testes automatizados focados em contrato HTTP e regras criticas de negocio (voto unico, sessao aberta/encerrada, apuracao de resultado, validacao de entrada).
- Refatoracoes guiadas por simplicidade: prioridade em clareza e robustez sem adicionar complexidade desnecessaria.

## Execucao local
### Requisitos
- Java 25+
- Docker + Docker Compose

### Comandos de execucao
> Opcional: personalizar variaveis de ambiente alterando o arquivo `.env`.

#### Subir o banco:
```bash
docker compose up -d
``` 

#### Rodar a API (Windows/PowerShell, na raiz do projeto):
```powershell
.\mvnw.cmd spring-boot:run
``` 

#### Rodar a API (Linux/macOS, na raiz do projeto):
```bash
./mvnw spring-boot:run
``` 

#### Validacao rapida (API no ar)
Windows/PowerShell:
```powershell
Invoke-RestMethod http://localhost:8080/v3/api-docs
``` 
Linux/macOS:
```bash
curl http://localhost:8080/v3/api-docs
``` 

#### Parar e remover volumes (reset de dados local):
```bash
docker compose down -v
```

## Swagger / OpenAPI
- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

## Endpoints e exemplos JSON
Caminho base: `/api/v1/pautas`

### 1) Criar pauta
`POST /api/v1/pautas`

Requisicao:
```json
{
  "titulo": "Reforma do Estatuto"
}
```

Resposta `201`:
```json
{
  "id": 1,
  "titulo": "Reforma do Estatuto",
  "createdAt": "2026-02-12T12:00:00Z"
}
```

### 2) Abrir sessao
`POST /api/v1/pautas/{pautaId}/sessoes`

Requisicao (duracao explicita):
```json
{
  "duracaoSegundos": 120
}
```

Requisicao (padrao 60s):
```json
{}
```

Resposta `201`:
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

Requisicao:
```json
{
  "associadoId": "assoc-001",
  "voto": "SIM"
}
```

Resposta `201`:
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

Resposta `200`:
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
### Comandos de teste
#### Executar testes unitarios e de contrato
Windows/PowerShell:
```powershell
.\mvnw.cmd test
``` 
Linux/macOS:
```bash
./mvnw test
```

#### Gerar cobertura com JaCoCo
Windows/PowerShell:
```powershell
.\mvnw.cmd verify
``` 
Linux/macOS:
```bash
./mvnw verify
```

#### Relatorio de cobertura
- `target/site/jacoco/index.html`

## Teste de performance
Teste de carga com k6 (via Docker, sem instalacao local):

Windows/PowerShell:
```powershell
docker run --rm -i `
  -v "${PWD}:/work" `
  -w /work `
  -e BASE_URL=http://host.docker.internal:8080 `
  grafana/k6 run perf/k6-votacao.js
```

Linux/macOS:
```bash
docker run --rm -i \
  -v "$(pwd):/work" \
  -w /work \
  -e BASE_URL=http://localhost:8080 \
  --network host \
  grafana/k6 run perf/k6-votacao.js
```

Criterios de sucesso esperados:
- `http_req_failed < 1%`
- `http_req_duration p(95) < 800ms`

Observacao: antes do teste, suba banco (`docker compose up -d`) e API (`.\mvnw.cmd spring-boot:run` no Windows ou `./mvnw spring-boot:run` no Linux/macOS).
Observacao Linux: `host.docker.internal` pode nao resolver; prefira `--network host` ou informe o IP da maquina em `BASE_URL`.

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
