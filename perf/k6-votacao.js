import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    voto_massivo: {
      executor: 'per-vu-iterations',
      vus: 50,
      iterations: 2000,
      maxDuration: '10m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

function jsonHeaders() {
  return { headers: { 'Content-Type': 'application/json' } };
}

export function setup() {
  const pautaResp = http.post(
    `${BASE_URL}/api/v1/pautas`,
    JSON.stringify({ titulo: 'carga spring' }),
    jsonHeaders()
  );

  const pautaOk = check(pautaResp, {
    'pauta criada': (r) => r.status === 201,
  });

  if (!pautaOk) {
    throw new Error(`Falha ao criar pauta. Status: ${pautaResp.status} Body: ${pautaResp.body}`);
  }

  const pautaId = pautaResp.json('id');

  const sessaoResp = http.post(
    `${BASE_URL}/api/v1/pautas/${pautaId}/sessoes`,
    JSON.stringify({ duracaoSegundos: 600 }),
    jsonHeaders()
  );

  const sessaoOk = check(sessaoResp, {
    'sessao criada': (r) => r.status === 201,
  });

  if (!sessaoOk) {
    throw new Error(`Falha ao abrir sessao. Status: ${sessaoResp.status} Body: ${sessaoResp.body}`);
  }

  return { pautaId };
}

export default function (data) {
  const unique = `${__VU}-${__ITER}`;
  const body = {
    associadoId: `assoc-${unique}`,
    voto: __ITER % 2 === 0 ? 'SIM' : 'NAO',
  };

  const voteResp = http.post(
    `${BASE_URL}/api/v1/pautas/${data.pautaId}/votos`,
    JSON.stringify(body),
    jsonHeaders()
  );

  check(voteResp, {
    'voto criado': (r) => r.status === 201,
  });

  if (__ITER % 50 === 0) {
    const resultResp = http.get(`${BASE_URL}/api/v1/pautas/${data.pautaId}/resultado`);
    check(resultResp, {
      'resultado consultado': (r) => r.status === 200,
    });
  }

  sleep(0.01);
}
