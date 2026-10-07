# New Relic no CheckVTR de homologação

A integração está preparada na branch `homolog`. O agente Java 9.4.0 só é carregado
quando `APM_ENABLED=true` e o perfil `homolog` está ativo. A configuração de conta,
o deploy e a coleta no servidor são etapas manuais do operador. Produção continua
fora deste experimento.

## 1 Conferir o recurso antes de publicar

No Coolify do **servidor separado de homologação**, confirmar:

- Recurso backend, branch `homolog`, build Docker Compose, base `/Viaturas` e serviço `app` na porta 8080.
- Perfis `docker,homolog`, datasource do PostgreSQL de homologação e mounts do banco atual.
- Banco e volume exclusivos. Manter o volume que já contém os dados fictícios.
- Domínio HTTPS que realmente recebe as chamadas de app e painel. O Compose e o guia
  do backend usam `back_homolog.pmto8bpm.com.br`; o guia da raiz também cita
  `homolog_back.pmto8bpm.com.br`. Essa informação precisa ser confirmada no Coolify.

Não modificar os domínios ou reconstruir os frontends só para instalar o APM.
Se a configuração implantada divergir do Compose no Git, comparar o Compose efetivo
antes de fazer deploy. Não mudar mounts, credenciais do PostgreSQL ou labels de
roteamento durante esta atividade. Uma divergência deve ser resolvida separadamente
antes de publicar, para manter a aplicação atual funcionando.

Registrar CPU, memória, sistema operacional, commit implantado e contagens do conjunto
fictício em `observability/results/experiment.json`, copiando o exemplo
`observability/experiment.example.json`. Não colocar credenciais nesse arquivo.

## 2 Criar a conta e preparar o backend

1. Criar conta em [New Relic](https://newrelic.com/signup) na modalidade gratuita.
   Conferir limites da conta e não contratar usuários ou recursos pagos para este teste.
2. Em API keys, obter a **license/ingest key**. Não usar uma user API key como chave do agente.
3. No recurso de homologação, adicionar `NEW_RELIC_LICENSE_KEY` como segredo de
   **runtime**. Não marcar como variável de build nem colocá-la em Dockerfile, Git ou relatório.
4. Adicionar `APM_ENABLED=false` inicialmente. O Compose repassa essas duas variáveis
   ao serviço `app`. Para implantação usando somente Dockerfile, informar também
   `SPRING_PROFILES_ACTIVE=homolog` e preservar a configuração de banco existente.
5. Publicar somente os arquivos desta integração na branch `homolog` após revisão.
   Se o Auto Deploy estiver ativo, o push já pode iniciar a reconstrução; por isso,
   conferir branch, banco, mounts e a flag desligada **antes** do push.
6. Fazer o deploy de homologação com o agente desligado e validar o login fictício.

A imagem baixa o agente do endereço oficial da versão 9.4.0 e verifica seu SHA256.
O script de inicialização preserva a JVM como processo principal do container.
Com APM desabilitado, o JAR do agente existe na imagem, mas não é carregado.

## 3 Executar a referência e ativar o APM

Executar os comandos no diretório do backend que contém `docker-compose.yml`.
Instalar o [k6 oficial](https://grafana.com/docs/k6/latest/set-up/install-k6/)
ou usar um executável portátil. A integração foi verificada com **k6 2.3.0**.

Substituir o endereço abaixo pelo domínio confirmado no passo 1. O script aceita
somente os dois candidatos de homologação, usa HTTPS e recusa redirecionamentos.
Nunca usar `--http-debug`, dumps de headers ou depuração de corpos: o login contém segredos.

```powershell
./observability/Run-Experiment.ps1 -BaseUrl https://back_homolog.pmto8bpm.com.br -Mode smoke
./observability/Run-Experiment.ps1 -BaseUrl https://back_homolog.pmto8bpm.com.br -Mode baseline
```

O script pede matrícula fictícia e senha oculta. As variáveis são restauradas ao
terminar. Se usar o k6 portátil, acrescentar `-K6Path C:/caminho/k6.exe`.
O modo smoke executa apenas uma sequência e verifica as quatro respostas HTTP 200.
A referência aquece por dois minutos e mede cinco minutos com um usuário.

Depois da referência, alterar **somente `APM_ENABLED=true`** no Coolify de homologação
e reimplantar o serviço `app`. Conferir no log de inicialização a versão 9.4.0 e o
carregamento da configuração; não copiar segredos ou dumps do ambiente.

Em New Relic > APM & services, procurar **checkvtr-api-homolog**. Fazer algumas
consultas normais e aguardar alguns minutos. Se não houver dados, conferir a chave
de ingestão, o log do agente e a saída HTTPS do container. Não abrir novas portas
de entrada ou expor endpoints de monitoramento na API.

```powershell
./observability/Run-Experiment.ps1 -BaseUrl https://back_homolog.pmto8bpm.com.br -Mode apm
./observability/Run-Experiment.ps1 -BaseUrl https://back_homolog.pmto8bpm.com.br -Mode unauthenticated
```

## 4 Metodologia e leitura dos resultados

O login é feito uma vez antes da carga, no formato real do backend: JSON com
`matricula` e `senha`, header `X-Client-Type: mobile` e token Bearer em memória.
Cada usuário repete as consultas a `/viaturas`, `/checklist?page=0&size=20`,
`/analytics/dashboard` e `/feed`, seguidas de pausa de um segundo.

| Modo | Aquecimento | Etapas de medição |
| --- | --- | --- |
| baseline | 2 min com 1 usuário | 5 min com 1 usuário |
| apm | 2 min com 1 usuário | 5 min com 1, depois 5 e depois 10 usuários |
| unauthenticated | nenhum | Uma chamada a `/viaturas` sem token |

Há dez segundos entre etapas para finalizar chamadas anteriores sem sobrepor cargas.
Após o primeiro minuto de cada etapa, o k6 avalia periodicamente a interrupção
se os erros inesperados ultrapassarem 5% ou o p95 ultrapassar 5000 ms.
Se houver 401/403 durante a carga autenticada, o teste é interrompido imediatamente.

- `client_duration_ms`: média, p95 e p99 do cliente, apenas da medição.
- `unexpected_errors`: qualquer resposta diferente de 200 nas quatro consultas.
- `measured_requests{level:1/5/10}`: volume por etapa; aquecimento e login ficam fora.
- RPS por etapa: contagem dividida por `stage_elapsed_seconds{level}.max`, aproximação
  do intervalo entre o início da etapa e a última sequência concluída.
- `responses_200`, `responses_401`, `responses_403`, `responses_404`, `responses_5xx`
  e `responses_other`: respostas da medição agrupadas por status.
- `http_*`: também inclui login e aquecimento; não usar sua média ou taxa global
  como se representasse somente os cinco minutos de uma etapa.
- `measurement_complete` e `thresholds_passed`: uma execução interrompida ou com
  falha de login não deve ser apresentada como coleta completa.

Os arquivos atuais `baseline.json`, `apm.json` e `unauthenticated.json` ficam em
`observability/results/`, com cópias datadas. Essa pasta é ignorada pelo Git e pelo
build Docker. O JSON exporta apenas métricas, sem token, corpo de login ou ambiente.
Uma nova execução atualiza o arquivo do modo e preserva a cópia anterior datada.

No APM, registrar duração das transações, throughput, taxa de erros, heap da JVM,
garbage collection e tempo das consultas JDBC. As respostas 401/403/404 são excluídas
da taxa de erros do agente e permanecem registradas separadamente no teste.
O tempo do cliente inclui rede e Traefik; não equivale à duração interna de uma
transação. O tempo JDBC inclui o caminho de acesso ao banco e não descreve sozinho
o desempenho interno do PostgreSQL.

Manter o mesmo conjunto fictício e a mesma versão da aplicação nas duas coletas.
O feed é cacheado em memória: o aquecimento reduz parte desse efeito. Uma comparação
única antes/depois não isola estatisticamente o custo do agente. Não atribuir uma
diferença pequena exclusivamente ao APM e não extrapolar o resultado para produção.

## 5 Capturas e documento

Salvar capturas em `observability/results/evidence/` com o período de medição visível:
resumo APM, transações, banco, JVM e situação dos erros. Conferir que não aparecem
nomes reais, CPF, tokens, cookies, headers ou segredos. SQL é ofuscado; logs da aplicação
não são encaminhados e mensagens de exceção são removidas. Não ativar profiler,
browser monitoring, infraestrutura ou logs automáticos nesta etapa.

Em `experiment.json`, registrar a data real, servidor, volume de dados fictícios,
participantes e observações. Em `apm.evidence_files`, usar caminhos relativos à pasta
results, por exemplo `evidence/resumo-apm.png`. Cada item de `findings` deve conter
uma conclusão sustentada pelas medições ou capturas. Confirmar os campos de isolamento,
implantação e privacidade somente depois da verificação no servidor.

O relatório inicial em `output/apm/` é **preliminar**: inclui o que foi verificado
localmente e declara pendentes o deploy e os resultados de homologação. Não apresentá-lo
como conclusão de uma coleta que ainda não aconteceu. O construtor em
`observability/report/build_report.py` lê os resultados reais, insere as capturas e
gera o Word atualizado. O roteiro está em `docs/APM-APRESENTACAO.md`.

Para atualizar no computador com Python e `python-docx` disponíveis:

```powershell
python observability/report/build_report.py --results observability/results --output ../../output/apm
./observability/report/Export-Report.ps1 -DocxPath ../../output/apm/Relatorio-APM-CheckVTR.docx
```

Usar um diretório de saída que exista ou possa ser criado. No workspace atual,
o comando acima aponta para `DoCheck/output/apm`. A exportação usa o Word instalado
em segundo plano. Se o Word não estiver disponível, abrir o DOCX em um editor com
exportação para PDF. Conferir visualmente o PDF atualizado; não reutilizar o PDF
preliminar após editar o Word.

## 6 Reversão

Se o backend não iniciar ou regredir, configurar `APM_ENABLED=false` e reimplantar
**somente `app`** no Coolify. Isso remove o argumento `-javaagent` da inicialização,
mantendo o mesmo banco e volume. Validar o login e uma listagem depois da reversão.
Se precisar voltar ao commit anterior, conferir novamente a definição de banco e
os mounts antes do deploy. Não executar remoção de volumes ou `down -v`.

## Verificações locais de 07/10/2026

- Java 21: 124 testes existentes passaram e o JAR foi empacotado.
- Docker Compose: validação da configuração passou com segredos fictícios.
- Inicialização: aplicação iniciou com e sem agente, usando H2 em memória; `/auth/me`
  sem autenticação retornou 401 nas duas execuções. O coletor foi direcionado para
  `127.0.0.1:9`, sem envio de telemetria ao New Relic.
- Proteção do entrypoint: sete cenários passaram, incluindo perfis indevidos e chave ausente.
- k6: cinco testes de configuração e cinco cenários contra HTTPS simulado localmente
  passaram. Foram verificadas falhas 500, login inválido, acesso sem token,
  redirecionamento recusado e ausência de credenciais nos arquivos de resultado.
- A construção da imagem Docker e a coleta New Relic não foram executadas: o daemon
  Docker local está indisponível e conta/deploy serão operados pelo usuário.

## Referências

- [New Relic em Docker](https://docs.newrelic.com/docs/apm/agents/java-agent/additional-installation/install-new-relic-java-agent-docker/)
- [Agente Java 9.4.0](https://docs.newrelic.com/docs/release-notes/agent-release-notes/java-release-notes/java-agent-940/)
- [Configuração e privacidade](https://docs.newrelic.com/docs/apm/agents/java-agent/configuration/java-agent-configuration-config-file/)
- [Compatibilidade Java](https://docs.newrelic.com/docs/apm/agents/java-agent/getting-started/compatibility-requirements-java-agent/)
- [Resumo personalizado k6](https://grafana.com/docs/k6/latest/results-output/end-of-test/custom-summary/)
- [Thresholds k6](https://grafana.com/docs/k6/latest/using-k6/thresholds/)
