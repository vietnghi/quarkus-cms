# Progress Log

## Phase 0 — Spike & decide  (IN PROGRESS)
- [x] Quarkiverse extension skeleton (runtime + deployment)
- [x] Code-first annotation model (@ContentType/@Field/@Relation/@RowPolicy/@TenantScoped)
- [x] Build-time @ContentType discovery stub (Jandex) in CmsProcessor
- [x] Dynamic `/api/{plural}` REST route + in-memory DocumentService (CRUD + draft/publish)
- [x] RLS, tenancy, workflow SPI/contracts stubbed (RowPolicyEnforcer, TenantResolver, WorkflowService)
- [x] Integration tests (JVM)
- [ ] Turn a discovered @ContentType class into a Panache entity (replace in-memory store)
- [ ] Dev Services: auto-start Postgres + seed two demo tenants
- [ ] Native IT green
- [ ] Finalize annotation API in ADR-0003 (done) + benchmark entity generation

## Phase 1 — Type Registry + Document Service + REST           (NOT STARTED)
## Phase 2 — Admin panel: Content-Type Builder (codegen) + CM   (NOT STARTED)
## Phase 3 — Auth, RBAC, Row-Level Security & Multi-tenancy     (NOT STARTED)
## Phase 4 — GraphQL API                                        (NOT STARTED)
## Phase 5 — Media Library                                      (NOT STARTED)
## Phase 6 — Draft/Publish, History, i18n & Workflow engine     (NOT STARTED)
## Phase 7 — Lifecycle hooks, webhooks, plugin SPI              (NOT STARTED)
## Phase 8 — Hardening, docs, native & release                  (NOT STARTED)
