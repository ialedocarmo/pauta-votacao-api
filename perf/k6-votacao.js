import http from 'k6/http';
import { check, sleep } from 'k6';

function intEnv(name, defaultValue) {
  const value = __ENV[name];
  if (!value) return defaultValue;
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? defaultValue : parsed;
}

function floatEnv(name, defaultValue) {
  const value = __ENV[name];
  if (!value) return defaultValue;
  const parsed = parseFloat(value);
  return Number.isNaN(parsed) ? defaultValue : parsed;
}

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = intEnv('VUS', 50);
const ITERATIONS = intEnv('ITERATIONS', 2000);
const MAX_DURATION = __ENV.MAX_DURATION || '10m';
const SESSION_DURATION_SECONDS = intEnv('SESSION_DURATION_SECONDS', 600);
const RESULT_EVERY_N_ITERATIONS = intEnv('RESULT_EVERY_N_ITERATIONS', 50);
const THINK_TIME_SECONDS = floatEnv('THINK_TIME_SECONDS', 0.01);
const THRESHOLD_P95_MS = intEnv('THRESHOLD_P95_MS', 800);
const FAIL_RATE = floatEnv('FAIL_RATE', 0.01);

export const options = {
  scenarios: {
    voto_massivo: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: ITERATIONS,
      maxDuration: MAX_DURATION,
    },
  },
  thresholds: {
    http_req_failed: [`rate<${FAIL_RATE}`],
    http_req_duration: [`p(95)<${THRESHOLD_P95_MS}`],
  },
};

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
    JSON.stringify({ duracaoSegundos: SESSION_DURATION_SECONDS }),
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

  if (RESULT_EVERY_N_ITERATIONS > 0 && __ITER % RESULT_EVERY_N_ITERATIONS === 0) {
    const resultResp = http.get(`${BASE_URL}/api/v1/pautas/${data.pautaId}/resultado`);
    check(resultResp, {
      'resultado consultado': (r) => r.status === 200,
    });
  }

  sleep(THINK_TIME_SECONDS);
}
