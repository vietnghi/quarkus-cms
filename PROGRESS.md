# Progress Log

## Phase 0 — Spike & decide  (COMPLETED)
- [x] Quarkiverse extension skeleton (runtime + deployment)
- [x] Code-first annotation model (@ContentType/@Field/@Relation/@RowPolicy/@TenantScoped)
- [x] Build-time @ContentType discovery stub (Jandex) in CmsProcessor
- [x] Dynamic `/api/{plural}` REST route + Panache-backed DocumentService
- [x] RLS, tenancy, workflow SPI/contracts stubbed
- [x] Integration tests (JVM) — 24/24 pass

## Phase 1 — Type Registry + Document Service + REST  (COMPLETED)
- [x] Build-time @ContentType discovery + SchemaRegistry registration via CmsRecorder
- [x] CmsEntry + CmsRelation Panache entities + Flyway V1 migration
- [x] PanacheDocumentService: CRUD, JSONB filter/sort, relation populate, field projection
- [x] ContentResource: Strapi-compatible filters/sort/pagination/fields/populate + {data,meta} envelope
- [x] Auto-generated OpenAPI via SmallRye OpenAPI
- [x] 12 ContentApi + 2 Relations tests covering all five query features

## Phase 2 — Admin panel: Content-Type Builder (codegen) + CM  (COMPLETED)
- [x] AdminResource at /cms-admin/api — content-types CRUD + entries CRUD + relations
- [x] CodegenResource — generates Java source files, registers types in SchemaRegistry
- [x] Static SPA at /cms-admin/index.html (vanilla HTML)
- [x] 5 AdminApi + 2 Codegen tests

## Phase 3 — Auth, RBAC, RLS & Multi-tenancy  (COMPLETED)
- [x] SecurityContext (per-request principal with userId, tenant, bypass)
- [x] SecurityContextProducer from HTTP headers (X-Tenant, X-User-Id, X-Bypass-RLS)
- [x] DefaultTenantResolver (reads X-Tenant header)
- [x] RowPolicyEnforcerImpl (programmatic tenant filtering)
- [x] SecuredDocumentService extends PanacheDocumentService
- [x] 1 SecurityTest

## Phase 4 — GraphQL API  (COMPLETED)
- [x] SmallRye GraphQL dependencies in runtime + deployment POMs
- [x] GraphQLAdapter with @GraphQLApi — contentTypes query + entry query
- [x] GraphQLEntry DTO for safe schema generation

## Phase 5 — Media Library  (COMPLETED)
- [x] StorageProvider SPI + LocalStorageProvider (disk-based)
- [x] MediaResource at /cms-admin/api/media (multipart upload)
- [x] ThumbnailService (placeholder)
- [x] 1 MediaTest + 1 StorageProviderTest

## Phase 6 — Workflow engine  (COMPLETED)
- [x] WorkflowService interface (current, transition, assign)
- [x] WorkflowServiceImpl (in-memory FSM with CDI EntryStageChanged events)

## Phase 7 — Webhooks & hooks  (COMPLETED)
- [x] WebhookService CDI bean (async HTTP callbacks)

## Phase 8 — Hardening  (COMPLETED)
- [x] 24/24 @QuarkusTest across 7 test classes
- [x] All reflection/serialization registered via ReflectiveClassBuildItem
- [x] Jandex indexing for runtime classes
- [x] NATIVE_BUILD.md with GraalVM instructions
- [x] README.md, DECISIONS.md, PROGRESS.md, CI workflow, .gitignore

**Final build:** ./mvnw verify: 24/24 tests pass — BUILD SUCCESS
**Native build:** ./mvnw verify -Dnative — code compatible through analysis (>6GB needed for compilation)
