# Backend em homologação

Produção permanece na branch `mota_branch`. Estas mudanças ficam na branch `homolog`.
Este guia não automatiza deploy, migração de banco ou criação de recursos AWS.
O bucket informado pelo usuário é `homolog-fotos-viaturas`, em `sa-east-1` (São Paulo).

## Coolify

- Branch: `homolog`, depois de publicar a branch.
- Build Pack **Docker Compose**. Base Directory: `/Viaturas`;
  Compose: `/docker-compose.yml` relativo a essa base. O build usa `/Viaturas/Dockerfile`.
- Domínio: `https://homolog_back.pmto8bpm.com.br`, serviço `app`, porta **8080**.
- Copiar os valores de `.env.homolog.example` para as variáveis da aplicação;
  **não** copiar os segredos ou configurações compartilhadas de produção.
- Preencher `POSTGRES_PASSWORD` com uma senha nova e `JWT_SECRET` com um segredo
  aleatório exclusivo (pelo menos 32 bytes para HS256). O Compose recusa valores vazios.
- O Compose fixa o perfil `docker,homolog`, usa `db:5432/frotapm_homolog` e usuário
  `frotapm_homolog`, sem containers globalmente nomeados.
- O volume `postgres-homolog-data` não é externo e recebe o escopo da stack/projeto.
  Os roteadores e serviços Traefik usam nomes exclusivos de homologação.
- CORS é tratado pelo Spring, incluindo `PATCH`, e permite somente os domínios de
  homologação configurados. Não duplicar o middleware CORS antigo do Traefik.
- Links de confirmação de e-mail e redefinição de senha usam `homolog_front`.

Se usar **Dockerfile** sem Compose, criar/configurar um PostgreSQL exclusivo e
informar `SPRING_PROFILES_ACTIVE=homolog`, `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `POSTGRES_PASSWORD`,
`POSTGRES_USER` e `JWT_SECRET`. Não usar o banco de produção.

## Antes de subir

1. Fazer backup de qualquer volume que já tenha dados, inclusive da homologação duplicada.
2. Conferir no Coolify se o volume do serviço `db` é diferente do de produção.
   A mudança de nome do volume cria um banco separado; não apaga nem migra o anterior.
   Não remover volumes antigos nem executar `docker compose down -v`.
3. Remover overrides copiados (`SPRING_DATASOURCE_*`, CORS, nomes Traefik, SMTP,
   URLs de e-mail e variáveis compartilhadas), substituindo pelos valores de teste.
4. O banco novo fica sem os dados iniciais de produção. Preparar batalhão, problemas
   e usuários autorizados com dados fictícios; não importar dados pessoais reais.
5. Configurar SMTP sandbox se testar cadastro/recuperação. O padrão `localhost:1025`
   não tem um servidor SMTP neste Compose: envio de e-mails falha até configurar um.

## Uploads

Todos os novos objetos de checklist e perfil recebem `homolog/` antes do caminho
existente. O perfil `homolog` exige prefixo não vazio. A URL de foto de perfil também
respeita o bucket e região configurados.

O bucket de homologação já criado é **`homolog-fotos-viaturas`**, região **`sa-east-1`**.
Ele é o padrão do
perfil e do Compose para ambos os tipos de foto, em `homolog/checklists/...` e
`homolog/usuarios/...`. A região foi confirmada como São Paulo.

- Usar credenciais IAM de teste limitadas a `homolog-fotos-viaturas/homolog/*`.
  O arquivo `s3-iam.homolog.example.json` é um exemplo de política de identidade,
  para revisar e vincular manualmente à identidade do backend de homologação.
  Prefixo sozinho separa os caminhos; a política IAM deve impedir acesso aos buckets reais.
- Configurar CORS S3 para permitir PUT a partir de `homolog_app` e `homolog_front`.
  O arquivo `s3-cors.homolog.example.json` é um exemplo para buckets exclusivos.
  Em buckets compartilhados, acrescentar a regra preservando as regras de produção.
- Não foi alterada nenhuma política, bucket ou chave na AWS por este ajuste.
- CORS não concede leitura pública. O fluxo atual exibe fotos com URLs diretas:
  revisar a política de leitura do bucket de teste para evitar respostas 403.
  Não expor dados pessoais reais; URLs presignadas para GET não foram implementadas.

## Validação após configuração manual

1. No log do backend, verificar perfil `homolog` ativo e banco exclusivo.
2. No banco de homologação: `SELECT current_database(), current_user;` deve retornar
   os nomes de homologação, não os de produção.
3. Validar login com usuário fictício e confirmar cookies restritos ao host (não
   adicionar `Domain=.pmto8bpm.com.br`, que misturaria ambientes).
4. Confirmar que app e painel usam o backend de homologação antes de operações de escrita.
5. Validar checklist com fotos; a URL/objeto deve ter caminho `homolog/checklists/...`.
6. Validar notificações e PATCH de pendências. Produção deve continuar inalterada.

Referências:
- https://coolify.io/docs/applications/builds/docker-compose
- https://coolify.io/docs/core/persistent-storage/storage-mounts/volume-mounts
- https://docs.aws.amazon.com/AmazonS3/latest/userguide/enabling-cors-examples.html
- https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-policies-s3.html
