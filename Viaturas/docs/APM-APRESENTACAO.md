# Apresentação do APM no CheckVTR

Roteiro de sete minutos, sem slides. Abrir o relatório e o New Relic no mesmo período
da coleta. Usar as capturas do relatório se o painel não estiver disponível.
As falas abaixo devem ser completadas com resultados reais antes da apresentação.

## Primeiro minuto

Apresentar o CheckVTR como sistema de inspeção de viaturas. Explicar que o objetivo
foi observar o desempenho do backend Spring Boot implantado em homologação,
com banco PostgreSQL exclusivo e dados fictícios. Identificar os participantes.

## Minutos dois e três

Mostrar a arquitetura cliente → Traefik → backend → PostgreSQL. Explicar que um
agente dentro da JVM envia métricas e traces ao New Relic por HTTPS.
Mostrar `checkvtr-api-homolog` e a versão 9.4.0. Descrever a ativação restrita ao perfil
homolog e o desligamento por flag. Explicar que a chave fica no Coolify e que logs,
headers, parâmetros e mensagens de exceção não fazem parte da coleta planejada.
Não mostrar o editor de variáveis com segredos.

## Minutos quatro a seis

Explicar a referência de um usuário sem agente e as etapas de um, cinco e dez
usuários com agente, sempre depois de dois minutos de aquecimento.
Mostrar a tabela do relatório com média, p95, RPS e erros de cada etapa.
Explicar p95: 95% das requisições da amostra terminaram até esse tempo.
Mostrar a transação mais demorada e a participação das operações JDBC, conforme
os dados observados. Mostrar memória da JVM e garbage collection.
Relatar o bloqueio de uma chamada sem token, separando esse resultado dos erros
inesperados da aplicação. Não chamar um status 403 esperado de indisponibilidade.

Apontar um achado sustentado pelas evidências. Se não houve erro ou gargalo evidente,
declarar que o sistema permaneceu estável **na carga e na janela testadas**.
Se houve interrupção, apresentar o limite atingido e a duração parcial da coleta.
Não afirmar que o agente melhorou desempenho nem atribuir diferenças pequenas
ao custo do agente com base em uma única comparação.

## Sétimo minuto

Concluir o que a ferramenta permitiu observar e uma próxima investigação baseada
no achado real. Explicar as limitações: coleta curta, dados fictícios, carga pequena,
efeitos de cache e ausência de monitoramento interno completo do PostgreSQL.
Informar que a integração pode ser desligada sem alterar o banco.

## Preparação final

Confirmar participantes, data real e domínio. Exportar o relatório atualizado para
PDF, conferir as capturas e ensaiar uma vez com cronômetro. O documento preliminar
não contém medições de homologação e não deve ser apresentado como experimento concluído.
