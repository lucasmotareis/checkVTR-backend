const ALLOWED_TARGETS = new Set([
  'https://back_homolog.pmto8bpm.com.br',
  'https://homolog_back.pmto8bpm.com.br',
]);

export function loadConfig(env) {
  const baseUrl = (env.BASE_URL || '').replace(/\/$/, '');
  const confirmedUrl = (env.CONFIRMED_HOMOLOG_URL || '').replace(/\/$/, '');
  if (!ALLOWED_TARGETS.has(baseUrl) || confirmedUrl !== baseUrl) {
    throw new Error('Confirme o dominio efetivo de homologacao e use BASE_URL igual a CONFIRMED_HOMOLOG_URL. Producao, IPs e outros destinos sao recusados.');
  }
  const mode = env.MODE || 'smoke';
  if (!['smoke', 'baseline', 'apm', 'unauthenticated'].includes(mode)) {
    throw new Error('MODE deve ser smoke, baseline, apm ou unauthenticated.');
  }
  if (mode !== 'unauthenticated' && (!env.TEST_MATRICULA || !env.TEST_SENHA)) {
    throw new Error('Configure TEST_MATRICULA e TEST_SENHA de um usuario ficticio.');
  }
  return { baseUrl, mode };
}

export function loadOptions(mode) {
  const common = {
    // Nao seguir redirecionamentos: evita encaminhar login/token a outro host.
    maxRedirects: 0,
    discardResponseBodies: true,
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
    systemTags: ['status', 'method', 'name', 'scenario'],
  };
  if (mode === 'unauthenticated') {
    return { ...common, scenarios: { acesso_sem_token: { executor: 'shared-iterations', vus: 1, iterations: 1, exec: 'unauthenticated' } },
      thresholds: { checks: ['rate==1'] } };
  }
  if (mode === 'smoke') {
    return { ...common, scenarios: { smoke: { executor: 'shared-iterations', vus: 1, iterations: 1, exec: 'measure' } },
      thresholds: { unexpected_errors: ['rate==0'], checks: ['rate==1'] } };
  }
  const scenarios = {
    aquecimento: { executor: 'constant-vus', vus: 1, duration: '2m', exec: 'warmup', gracefulStop: '0s' },
    medicao_1: { executor: 'constant-vus', vus: 1, duration: '5m', startTime: '2m', exec: 'measure', gracefulStop: '10s', tags: { level: '1' } },
  };
  const levels = ['1'];
  if (mode === 'apm') {
    // Intervalo permite terminar requisicoes anteriores sem sobrepor cargas.
    scenarios.medicao_5 = { executor: 'constant-vus', vus: 5, duration: '5m', startTime: '7m10s', exec: 'measure', gracefulStop: '10s', tags: { level: '5' } };
    scenarios.medicao_10 = { executor: 'constant-vus', vus: 10, duration: '5m', startTime: '12m20s', exec: 'measure', gracefulStop: '10s', tags: { level: '10' } };
    levels.push('5', '10');
  }
  const thresholds = {};
  for (const level of levels) {
    // Avaliacao periodica; so interrompe apos um minuto de cada etapa.
    const delay = { '1': '3m', '5': '8m10s', '10': '13m20s' }[level];
    thresholds[`unexpected_errors{level:${level}}`] = [{ threshold: 'rate<=0.05', abortOnFail: true, delayAbortEval: delay }];
    thresholds[`client_duration_ms{level:${level}}`] = [{ threshold: 'p(95)<=5000', abortOnFail: true, delayAbortEval: delay }];
    thresholds[`measured_requests{level:${level}}`] = ['count>0'];
    thresholds[`stage_elapsed_seconds{level:${level}}`] = ['max>=0'];
  }
  for (const route of ['viaturas', 'checklists', 'dashboard', 'feed']) {
    thresholds[`client_duration_ms{route:${route}}`] = ['p(95)<=5000'];
  }
  return { ...common, scenarios, thresholds };
}
