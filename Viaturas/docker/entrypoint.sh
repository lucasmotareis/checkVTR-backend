#!/bin/sh
set -eu

# Sem flag, a JVM inicia sem carregar o agente (inclusive em rollback).
case "${APM_ENABLED:-false}" in
  false) exec java -jar /app/app.jar "$@" ;;
  true) ;;
  *) echo 'APM_ENABLED deve ser true ou false.' >&2; exit 64 ;;
esac

case ",${SPRING_PROFILES_ACTIVE:-}," in
  *,homolog,*) ;;
  *) echo 'APM recusado: o perfil homolog precisa estar ativo.' >&2; exit 64 ;;
esac
case ",${SPRING_PROFILES_ACTIVE:-}," in
  *,prod,*|*,production,*) echo 'APM recusado: perfil de producao ativo.' >&2; exit 64 ;;
esac
if [ -z "${NEW_RELIC_LICENSE_KEY:-}" ]; then
  echo 'APM recusado: configure a chave de ingestao New Relic no runtime.' >&2
  exit 64
fi

exec java -javaagent:/opt/newrelic/newrelic.jar \
  -Dnewrelic.environment=homolog \
  -Dnewrelic.config.file=/opt/newrelic/newrelic.yml \
  -jar /app/app.jar "$@"
