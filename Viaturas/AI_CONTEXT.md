# Project: CheckVTR

Purpose:
Vehicle inspection system for Military Police.

Backend:
Spring Boot 3
Java 21
PostgreSQL
JWT authentication

Frontend:
React Native (Expo)

Infrastructure:
Docker
AWS
Traefik
Coolify

Architecture:
Controller -> Service -> Repository

Application style:
- Modular monolith (package-by-domain)
- Stateless API with JWT
- Battalion-scoped data access (multi-tenant by `batalhao_id`)

Main backend modules:
- `auth`: login/register/logout/me, JWT generation and validation
- `users`: authenticated user profile updates (photo URL, notification seen marker)
- `viaturas`: vehicle CRUD and vehicle badge counters
- `checklists`: checklist creation, checklist problems, chief acknowledgment
- `notifications`: battalion notifications, unread counters, stats
- `analytics`: aggregated dashboards and ranking queries
- `feed`: recent activity feed in cache
- `storage`: S3 presigned upload URL generation
- `common/config`: exception handling, CORS, cache, swagger, security setup

Request lifecycle:
1. Request enters Spring Security filter chain.
2. `JwtAuthFilter` reads token from `Authorization: Bearer` or `token` cookie.
3. User is loaded via `UserDetailsService` and placed in SecurityContext.
4. Controller extracts authenticated user and forwards to service.
5. Service enforces role and battalion ownership rules.
6. Repository executes JPA/native queries in PostgreSQL.
7. DTO/response is returned.

Security model:
- Public routes: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- Protected routes: all remaining endpoints
- Roles:
  - `MOTORISTA`
  - `CHEFE_TRANSPORTE` (required for administrative actions)
- Session policy: STATELESS

Core data model:
- `Batalhao` (organization unit)
- `User` -> many-to-one `Batalhao`
- `Viatura` -> many-to-one `Batalhao`
- `CheckList` -> many-to-one `Viatura`, many-to-one `User`
- `CheckListProblema` -> many-to-one `CheckList`, many-to-one `Problema`
- `Notification` -> many-to-one `Batalhao`
- Additional auth support table: `CadastroAutorizado`

Persistence and queries:
- Spring Data JPA repositories per domain
- JPQL projections for badges and analytics
- Native SQL for monthly checklist rollups
- Pagination in checklist and notification listing endpoints

Caching:
- Caffeine cache manager
- Key cache: `ultimosChecklists` used by feed service
- Feed stores last events per battalion in memory

Storage integration:
- AWS S3 presigned PUT URLs for checklist images and profile photos
- Upload flow:
  1. Backend generates presigned URL + key
  2. Client uploads directly to S3
  3. Client confirms file key in backend when needed

Deployment layout:
- Multi-stage Docker build (Maven build + JRE runtime image)
- `docker-compose` services:
  - `app` (Spring Boot API)
  - `db` (PostgreSQL 15)
- Traefik routes HTTPS traffic to app container

Known technical debt to watch:
- Some endpoints return entities directly instead of DTOs
- Package naming is not fully consistent (`storage/dto` contains controller)
- `pom.xml` has Java 21 property but compiler plugin source/target set to 9
- Auth repository/service contains a return type mismatch in `findByCpfAndMatricula`

Coding rules:
- Always use DTOs
- Use MapStruct
- Validate requests
