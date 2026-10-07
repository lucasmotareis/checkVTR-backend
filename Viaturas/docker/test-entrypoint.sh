#!/bin/sh
# Verifica o comando efetivamente passado a JVM sem iniciar Java ou acessar rede.
set -eu
script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
test_dir=$(mktemp -d)
trap 'rm -f "$test_dir/java"; rmdir "$test_dir"' EXIT HUP INT TERM
printf '#!/bin/sh\nprintf "%%s\\n" "$@"\n' > "$test_dir/java"
chmod +x "$test_dir/java"
export PATH="$test_dir:$PATH"
unset APM_ENABLED NEW_RELIC_LICENSE_KEY SPRING_PROFILES_ACTIVE

output=$(sh "$script_dir/entrypoint.sh")
case "$output" in *-javaagent*) exit 1 ;; esac
echo 'OK default: sem agente'

if APM_ENABLED=true NEW_RELIC_LICENSE_KEY=chave-ficticia SPRING_PROFILES_ACTIVE=production sh "$script_dir/entrypoint.sh" > /dev/null 2>&1; then exit 1; fi
echo 'OK producao recusada'
if APM_ENABLED=true NEW_RELIC_LICENSE_KEY=chave-ficticia SPRING_PROFILES_ACTIVE=homolog,production sh "$script_dir/entrypoint.sh" > /dev/null 2>&1; then exit 1; fi
echo 'OK perfis misturados recusados'
if APM_ENABLED=true NEW_RELIC_LICENSE_KEY=chave-ficticia SPRING_PROFILES_ACTIVE=homologacao sh "$script_dir/entrypoint.sh" > /dev/null 2>&1; then exit 1; fi
echo 'OK perfil parecido recusado'
if APM_ENABLED=true SPRING_PROFILES_ACTIVE=homolog sh "$script_dir/entrypoint.sh" > /dev/null 2>&1; then exit 1; fi
echo 'OK chave ausente recusada'
if APM_ENABLED=TRUE sh "$script_dir/entrypoint.sh" > /dev/null 2>&1; then exit 1; fi
echo 'OK flag invalida recusada'
output=$(APM_ENABLED=true NEW_RELIC_LICENSE_KEY=chave-ficticia SPRING_PROFILES_ACTIVE=docker,homolog sh "$script_dir/entrypoint.sh" --server.port=8081)
case "$output" in *-javaagent:/opt/newrelic/newrelic.jar*) ;; *) exit 1 ;; esac
case "$output" in *-Dnewrelic.environment=homolog*) ;; *) exit 1 ;; esac
case "$output" in *--server.port=8081*) ;; *) exit 1 ;; esac
case "$output" in *chave-ficticia*) exit 1 ;; esac
echo 'OK homolog: argumento do agente, configuracao e argumentos da aplicacao preservados; chave fora dos argumentos'
