# Architecture Decision Records

## ADR-0001 — Extension shape: runtime + deployment modules
**Status:** Accepted
**Decision:** Ship as a standard Quarkiverse extension with separate `runtime` and
`deployment` modules. All CDI bean and reflection registration happens in
`CmsProcessor` build steps.

## ADR-0002 — Dynamic dispatch for the content API
**Status:** Accepted
**Decision:** A single templated resource `/api/{plural}` resolves the content type
from the registry and delegates to the `DocumentService`. GraphQL (Phase 4) follows
the same single-engine pattern. Every data access goes through the Document Service
so RLS + tenant filters cannot be bypassed.

## ADR-0003 — Code-first content modeling (Java classes, not schema.json)
**Status:** Accepted (Revision 2)
**Context:** Idiomatic Quarkus is build-time, type-safe, and code-first. A runtime
JSON-schema store fights the framework and weakens typing, queries, RLS and native.
**Decision:** Content types are **annotated Java classes** (`@ContentType`, `@Field`,
`@Relation`, `@RowPolicy`, `@TenantScoped`) under a configured package, discovered at
build time via Jandex and turned into Panache entities. The admin Content-Type Builder
is a **source-code generator** (writes/updates those Java files + live reload), never a
second source of truth. An optional runtime "overlay" store may exist as a documented,
non-default escape hatch.

## ADR-0004 — Row-Level Security as a single choke point
**Status:** Accepted (Revision 2)
**Decision:** RLS is enforced for every read/write inside the `DocumentService` via a
`RowPolicyEnforcer` that enables Hibernate filters from the per-request
`SecurityContext` (identity + roles + tenant). `@RowPolicy` predicates may reference
`:currentUserId`, `:currentTenant`, and roles. Optional PostgreSQL native RLS policies
provide defense in depth. No adapter (REST/GraphQL/admin/export) bypasses it.

## ADR-0005 — Multi-tenancy via Hibernate ORM multitenancy
**Status:** Accepted (Revision 2)
**Decision:** First-class multi-tenant mode using Hibernate ORM multitenancy with a
pluggable `TenantResolver` SPI. Strategy is configurable: `DISCRIMINATOR` (default,
shared schema + `tenant_id`, composes with RLS), `SCHEMA`, or `DATABASE`. The tenant
predicate is injected by the same filter layer as RLS so RLS operates *within* a
tenant and cross-tenant access is structurally impossible. Default deny.

## ADR-0006 — Basic workflow engine (role-gated FSM, not BPMN)
**Status:** Accepted (Revision 2)
**Decision:** A built-in finite state machine (`WorkflowService`) over stage/transition
metadata enforces legal, role-permitted transitions and fires `EntryStageChanged` CDI
events (consumed by webhooks/notifications). Tenant-scoped and RLS-aware. Heavier
orchestration (Kogito/Flowable) is deferred and can slot behind the same interface.
