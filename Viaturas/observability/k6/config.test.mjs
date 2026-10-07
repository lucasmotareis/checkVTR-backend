import test from 'node:test';
import assert from 'node:assert/strict';
import { loadConfig, loadOptions } from './config.mjs';

const valid = { BASE_URL: 'https://back_homolog.pmto8bpm.com.br', CONFIRMED_HOMOLOG_URL: 'https://back_homolog.pmto8bpm.com.br', TEST_MATRICULA: 'ficticio', TEST_SENHA: 'somente-teste' };

test('recusa producao, URLs com credenciais, portas, subdominios enganosos e IPs', () => {
  for (const url of ['https://api.pmto8bpm.com.br', 'http://back_homolog.pmto8bpm.com.br', 'https://back_homolog.pmto8bpm.com.br.evil.test', 'https://user:senha@back_homolog.pmto8bpm.com.br', 'https://back_homolog.pmto8bpm.com.br:8443', 'https://127.0.0.1', 'https://back_homolog.pmto8bpm.com.br/path']) {
    assert.throws(() => loadConfig({ ...valid, BASE_URL: url, CONFIRMED_HOMOLOG_URL: url }));
  }
});
test('requer confirmacao do mesmo dominio e credenciais fora da falha controlada', () => {
  assert.throws(() => loadConfig({ ...valid, CONFIRMED_HOMOLOG_URL: '' }));
  assert.throws(() => loadConfig({ ...valid, CONFIRMED_HOMOLOG_URL: 'https://homolog_back.pmto8bpm.com.br' }));
  assert.throws(() => loadConfig({ ...valid, TEST_SENHA: '' }));
  assert.equal(loadConfig({ ...valid, MODE: 'unauthenticated', TEST_SENHA: '' }).mode, 'unauthenticated');
  assert.equal(loadConfig({ ...valid, BASE_URL: valid.BASE_URL + '/' }).baseUrl, valid.BASE_URL);
});
test('modo desconhecido nao inicia carga e nenhum modo segue redirecionamentos', () => {
  assert.throws(() => loadConfig({ ...valid, MODE: 'stress' }));
  for (const mode of ['baseline', 'apm', 'smoke', 'unauthenticated']) assert.equal(loadOptions(mode).maxRedirects, 0);
});
test('referencia nao escala carga e aquecimento nao usa a funcao de medicao', () => {
  const options = loadOptions('baseline');
  assert.deepEqual(Object.keys(options.scenarios), ['aquecimento', 'medicao_1']);
  assert.equal(options.scenarios.aquecimento.exec, 'warmup');
  assert.equal(options.scenarios.medicao_1.startTime, '2m');
});
test('APM limita dez usuarios e permite um minuto antes de interromper cada etapa', () => {
  const options = loadOptions('apm');
  assert.equal(Math.max(...Object.values(options.scenarios).map(s => s.vus)), 10);
  for (const [level, delay] of [['1', '3m'], ['5', '8m10s'], ['10', '13m20s']]) {
    assert.equal(options.thresholds[`unexpected_errors{level:${level}}`][0].delayAbortEval, delay);
    assert.equal(options.thresholds[`client_duration_ms{level:${level}}`][0].abortOnFail, true);
  }
});
