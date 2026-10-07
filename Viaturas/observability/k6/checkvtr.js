import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import exec from 'k6/execution';
import { loadConfig, loadOptions } from './config.mjs';

const config = loadConfig(__ENV);
export const options = loadOptions(config.mode);
const duration = new Trend('client_duration_ms', true);
const failures = new Rate('unexpected_errors');
const measuredRequests = new Counter('measured_requests');
const elapsedSeconds = new Trend('stage_elapsed_seconds');
const statusCounts = Object.fromEntries(['200', '401', '403', '404', '5xx', 'other'].map(status => [status, new Counter(`responses_${status}`)]));
const routes = [
  ['viaturas', '/viaturas'],
  ['checklists', '/checklist?page=0&size=20'],
  ['dashboard', '/analytics/dashboard'],
  ['feed', '/feed'],
];

export function setup() {
  console.info(`Modo=${config.mode}; alvo=${config.baseUrl}; inicio=${new Date().toISOString()}; fuso de analise=America/Sao_Paulo`);
  if (config.mode === 'unauthenticated') return {};
  const response = http.post(`${config.baseUrl}/auth/login`, JSON.stringify({
    matricula: __ENV.TEST_MATRICULA, senha: __ENV.TEST_SENHA,
  }), {
    headers: { 'Content-Type': 'application/json', 'X-Client-Type': 'mobile' },
    responseType: 'text', timeout: '10s', tags: { name: 'POST /auth/login', phase: 'setup' },
  });
  let token;
  try { token = response.json('token'); } catch (_) { /* Nunca imprimir corpo de login. */ }
  if (response.status !== 200 || typeof token !== 'string' || !token) {
    fail(`Login ficticio falhou (HTTP ${response.status}); carga nao iniciada.`);
  }
  // Token permanece apenas na memoria do k6, nao no arquivo de resultados.
  return { token };
}

function journey(data, measured) {
  const level = exec.scenario.name.replace('medicao_', '');
  for (const [route, path] of routes) {
    const response = http.get(`${config.baseUrl}${path}`, {
      headers: { Authorization: `Bearer ${data.token}` }, timeout: '10s',
      tags: { name: `GET ${path.split('?')[0]}`, phase: measured ? 'measurement' : 'warmup', route, level },
    });
    const ok = response.status === 200;
    if (measured) {
      const tags = { route, level };
      duration.add(response.timings.duration, tags);
      failures.add(!ok, tags);
      measuredRequests.add(1, tags);
      const status = String(response.status);
      const bucket = statusCounts[status] ? status : response.status >= 500 && response.status < 600 ? '5xx' : 'other';
      statusCounts[bucket].add(1, tags);
      check(response, { [`${route} retorna 200`]: () => ok });
    }
    if (response.status === 401 || response.status === 403) {
      exec.test.abort('Sessao invalida ou acesso negado: interrompido para nao medir uma carga sem autenticacao.');
    }
  }
  if (measured) elapsedSeconds.add((Date.now() - exec.scenario.startTime) / 1000, { level });
  sleep(1);
}

export function warmup(data) { journey(data, false); }
export function measure(data) { journey(data, true); }

export function unauthenticated() {
  const response = http.get(`${config.baseUrl}/viaturas`, {
    timeout: '10s', tags: { name: 'GET /viaturas sem token', phase: 'controlled' },
    responseCallback: http.expectedStatuses(401, 403),
  });
  const status = String(response.status);
  (statusCounts[status] || statusCounts.other).add(1);
  check(response, { 'acesso sem token bloqueado': r => r.status === 401 || r.status === 403 });
  console.info(`Falha controlada: HTTP ${response.status}. Registrar fora da taxa de erros inesperados.`);
}

export function handleSummary(data) {
  const finished = new Date();
  const durationMs = data.state?.testRunDurationMs || 0;
  const rawThresholdsPassed = Object.values(data.metrics).every(metric =>
    Object.values(metric.thresholds || {}).every(threshold => threshold.ok));
  const total = data.metrics.measured_requests?.values?.count || 0;
  const complete = config.mode === 'unauthenticated'
    ? data.metrics.checks?.values?.rate === 1
    : config.mode === 'smoke' ? total >= 4
    : durationMs >= (config.mode === 'baseline' ? 420000 : 1040000)
      && (config.mode === 'baseline' ? ['1'] : ['1', '5', '10']).every(level =>
        (data.metrics[`measured_requests{level:${level}}`]?.values?.count || 0) > 0);
  const thresholdsPassed = rawThresholdsPassed && complete;
  // Serializar apenas metricas: nunca exportar setup_data, env, headers ou corpos.
  const result = {
    schema_version: 1, mode: config.mode, base_url: config.baseUrl,
    started_at_utc: new Date(finished.getTime() - durationMs).toISOString(),
    finished_at_utc: finished.toISOString(), timezone: 'America/Sao_Paulo',
    test_duration_ms: durationMs, measurement_complete: complete, thresholds_passed: thresholdsPassed,
    metrics: data.metrics,
    notes: [
      'client_duration_ms, unexpected_errors e measured_requests excluem login e aquecimento.',
      'http_* inclui setup e aquecimento. Calcular RPS por etapa usando measured_requests{level} / stage_elapsed_seconds{level}.max; aproxima o intervalo ate a ultima sequencia completa.',
      'Latencia do cliente inclui rede e proxy; nao equivale ao tempo de transacao APM.',
      'As cargas de 5 e 10 usuarios possuem limites de interrupcao proprios.',
    ],
  };
  const text = JSON.stringify(result, null, 2);
  return { [`observability/results/${config.mode}.json`]: text,
    stdout: `\nResultado ${config.mode}: criterios ${thresholdsPassed ? 'atendidos' : 'nao atendidos'}. JSON salvo em observability/results/${config.mode}.json\n` };
}
