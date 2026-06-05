# Quarkus CMS Extension — Hermes Goal-Mode Brief

> **Codename:** `quarkus-cms` (a Strapi-inspired headless CMS, delivered as a Quarkus extension)
> **Input format:** This document is written to be pasted into **Hermes Agent (goal mode)**. The agent reads the **Goal**, operates within the **Constraints**, and works through the **Phased Plan** milestone by milestone, treating each milestone's **Acceptance Criteria** as the definition of done before advancing.
> **Revision 2** — code-first Java modeling, row-level security, multi-tenant deployment, and a basic workflow engine are now first-class.

---

## 0. How Hermes Should Use This Document

1. Treat **Section 1 (North-Star Goal)** as the single objective. Everything else is scaffolding to reach it.
2. Work **one phase at a time, in order**. Do not start Phase N+1 until every acceptance criterion in Phase N passes (compiles, tests green, demo works).
3. After each phase, **self-report**: what was built, what tests prove it, what is deferred, and the diff/PR summary.
4. Obey every rule in **Section 2 (Constraints & Guardrails)** at all times. When a constraint and a feature conflict, the constraint wins — surface the conflict instead of silently breaking the rule.
5. When a decision is ambiguous, prefer the **idiomatic Quarkus path** (code-first, build-time processing, CDI, MicroProfile/SmallRye, Dev Services) over re-implementing Strapi's Node.js internals literally.
6. Keep a running `DECISIONS.md` (architecture decision records) and `PROGRESS.md` in the repo root.

---

## 1. North-Star Goal

**Build `quarkus-cms`: an open-source, headless Content Management System delivered as a Quarkus extension, that gives a Java/Quarkus developer the Strapi experience — model content as type-safe Java classes, get an auto-generated, secured REST + GraphQL API instantly, manage entries and media, and control access with roles, row-level security, and per-tenant isolation — while remaining fully cloud-native (fast startup, low memory, GraalVM-native-image compatible, build-time optimized).**

A developer should be able to:

```bash
quarkus create app com.acme:my-cms
quarkus ext add io.quarkiverse.cms:quarkus-cms
# define content types as annotated Java classes under com.acme.cms.types
./mvnw quarkus:dev
# -> opens an admin panel at /cms-admin
# -> the "Article" class is instantly a secured REST resource at /api/articles
# -> query it via /graphql, upload media, assign roles & row-level policies,
#    move entries through a review workflow, publish drafts — no controller code
```

The end state is **feature-comparable to Strapi's core CMS — plus enterprise concerns Strapi charges for (RLS, multi-tenancy, review workflows)** — but native to the Quarkus/Jakarta EE ecosystem rather than Node.js.

---

## 2. Constraints & Guardrails

**Framework & runtime**
- MUST be a proper **Quarkus extension** with `runtime` and `deployment` modules, using `@BuildStep`, `@Recorder`, and `*BuildItem`s. No "it's just a library" shortcut.
- MUST support **JVM and GraalVM native image** builds. Anything reflective (JSON (de)serialization, dynamic queries) MUST register reflection/serialization at build time via build items.
- MUST preserve Quarkus value props: sub-second dev startup, live reload, low memory. Discover content types at **build time** (Jandex index) rather than runtime classpath scanning.
- Target **Quarkus LTS (3.x current LTS)**, Java 17+ (prefer 21), Jakarta EE 10 namespaces (`jakarta.*`).

**Content modeling — code-first (per revision)**
- The **source of truth for content types is annotated Java classes** under a configurable package (e.g. `com.acme.cms.types`), NOT JSON schema files. This is the idiomatic Quarkus path — content types are discovered at build time exactly like JPA entities and JAX-RS resources.
- The admin Content-Type Builder UI operates as **dev-time codegen** (it generates/edits those Java source files and triggers live reload), not a parallel runtime schema store. Code stays the single, version-controlled source of truth.

**Architecture**
- Persistence via **Hibernate ORM with Panache** (default) + **Hibernate Reactive** path where feasible; SQL-first, real typed columns (not JSONB blobs). Support PostgreSQL (primary), MySQL/MariaDB, H2 (dev).
- REST via **Quarkus REST (RESTEasy Reactive)**; GraphQL via **SmallRye GraphQL**; security via **Quarkus Security + SmallRye JWT / Elytron**; validation via **Hibernate Validator**; JSON via Jackson.
- **Row-Level Security (RLS)** is a cross-cutting requirement: every read/write through the Document Service is filtered by the caller's identity, roles, ownership, and tenant. Enforced at the query layer (Hibernate filters / Panache query augmentation) with optional push-down to **PostgreSQL native RLS policies** for defense in depth.
- **Multi-tenancy** is a first-class deployment mode using **Hibernate ORM multitenancy** (DATABASE / SCHEMA / DISCRIMINATOR strategies) with a pluggable `TenantResolver`. Single-tenant is the default, but tenant isolation must never be an afterthought retrofit.
- Provide **Dev Services** (auto-start Postgres + seed admin/tenant) and **Dev UI** panels for the CMS.

**Engineering quality**
- Test pyramid: unit (JUnit 5) + `@QuarkusTest` integration + `@QuarkusIntegrationTest` for native. Testcontainers for DB. Every endpoint and every security/tenant boundary gets a test (including negative allow/deny and cross-tenant leakage tests).
- Public API surface documented with OpenAPI (SmallRye OpenAPI) auto-generated.
- Semantic versioning, conventional commits, CI that runs JVM + native verification.
- Do **not** copy Strapi (MIT/EE) source code — re-implement from feature understanding.

**Scope discipline**
- "Full platform" is the destination, but it is reached **incrementally**. Never let a later-phase feature block a working earlier-phase demo.
- No bespoke frontend framework: the admin panel is a single SPA (React or Lit) served as static resources from the extension; keep it thin and API-driven.

---

## 3. Deep Analysis of Strapi (what we are matching)

Strapi defines the modern "developer-first headless CMS" UX. Below is the feature decomposition Hermes should treat as the **functional spec**, each item annotated with how it should be realized in Quarkus. Where this brief deliberately diverges from Strapi (code-first modeling, built-in RLS/tenancy/workflow), that is called out.

### 3.1 Content modeling — *the heart of the system* — **code-first Java**
- **What Strapi does:** a visual Content-Type Builder that defines models stored as `schema.json` files + DB reflection, with **Collection Types** (many entries), **Single Types** (one entry), **Components** (reusable field groups), and **Dynamic Zones** (ordered lists of mixed components).
- **Field types to match:** text, rich text/blocks, number, boolean, date/time, email, password, enumeration, JSON, UID/slug, media, relation, component, dynamic zone.
- **Relations to match:** one-to-one, one-to-many, many-to-one, many-to-many, polymorphic.

> **Quarkus mapping (revised — code-first):** Content types are **annotated Java classes under a configured package**, discovered at build time via a Jandex `@BuildStep` — the same mechanism Quarkus uses for entities and resources. This replaces `schema.json` as the source of truth and gives type safety, refactoring, IDE support, real SQL columns, and native-image friendliness for free.
>
> Proposed annotation model (Hermes designs the final API in Phase 0):
> ```java
> @ContentType(api = "article", kind = COLLECTION, draftAndPublish = true)
> @TenantScoped                         // see 3.10
> @RowPolicy(name = "own-articles",     // see RLS, 3.3/3.6
>            expression = "author.id = :currentUserId",
>            appliesTo = {READ, UPDATE, DELETE},
>            roles = "author")
> public class Article {
>     @Field(required = true) String title;
>     @Uid(from = "title")    String slug;
>     @Field(localized = true) @RichText String body;
>     @Media Asset cover;
>     @Relation(MANY_TO_ONE) Author author;
>     @Component Seo seo;                       // reusable field group
>     @DynamicZone(of = {Hero.class, Gallery.class, Quote.class}) List<Object> layout;
> }
> ```
> - **Components** → embeddable Java types (`@Component`/`@Embeddable`-style). **Dynamic Zones** → `@DynamicZone` over a closed set of component classes, persisted with a discriminator.
> - These classes are turned into **Panache entities** at build time (or augmented in place), so persistence, relations and queries are real Hibernate, not a generic store.
> - The **Content-Type Builder UI** becomes a **source-code generator**: editing a type in the admin panel writes/updates the Java file under the configured package and triggers `quarkus:dev` live reload. The UI never becomes a second source of truth.
> - For users who still want runtime-defined types without a rebuild, offer an **optional** dynamic-overlay store as a non-default escape hatch — documented as a trade-off, not the primary path.

### 3.2 Auto-generated Content API
- **REST API:** every content type instantly exposes `GET/POST/PUT/DELETE /api/{plural}`, with `filters`, `sort`, `pagination`, `populate`, `fields`, `locale`, `status`. Standardized `{ data, meta }` envelope.
- **GraphQL API:** the same models auto-exposed with the same semantics.
- **Document Service concept (Strapi 5):** a unified internal service layer handling documents, components, dynamic zones, draft/publish variants, and locales — the single source of truth all APIs call into.

> **Quarkus mapping:** A **Document Service** CDI bean is the canonical CRUD + query engine over the discovered types. REST and GraphQL are thin adapters over it. **Every query the Document Service issues is wrapped by the RLS + tenant filters (3.3/3.6/3.10)** — adapters never bypass it. REST routing is dynamic (one templated resource dispatching by content-type name) registered at build time; GraphQL schema is generated from the discovered types. Mirror Strapi's query-param contract for familiarity.

### 3.3 Content management (the editor experience) — **with row-level security**
- **Content Manager:** admin CRUD UI auto-built from each model — list views, filters, edit forms with the right widget per field type.
- **Draft & Publish:** every entry has draft + published variants; two-tab editor; explicit publish action.
- **Content History / versioning:** view and restore previous versions of an entry.
- **Preview:** preview unpublished content via a frontend preview URL.

> **Quarkus mapping + RLS:** Draft/publish = status column + published snapshot in the Document Service. History = append-only `cms_entry_version` table. Crucially, **the Content Manager only ever shows and edits rows the current user is permitted to see** — the same Row-Level Security filters that protect the public API also scope the admin list views, counts, and edit access. RLS is implemented as:
> - **Declarative row policies** (`@RowPolicy` on the type, or policy rows in the admin) expressing predicates like `author.id = :currentUserId`, `tenant_id = :currentTenant`, `status = 'published' OR role = 'editor'`.
> - Enforced via **Hibernate filters** (`@FilterDef`/`@Filter`) enabled per request from the security identity, plus Panache query augmentation in the Document Service.
> - Optional **PostgreSQL native RLS** (`CREATE POLICY`, session GUCs set per request) for defense in depth so even raw SQL is constrained.
> Editors with `bypassRowPolicy` permission can opt out where appropriate. Negative tests prove a user cannot read/update another user's or another tenant's rows.

### 3.4 Media Library
- Centralized asset repository: upload images/video/docs, folders, search, metadata, automatic responsive image variants/thumbnails, replace/crop.
- Pluggable storage providers (local disk, S3, Cloudinary, etc.).

> **Quarkus mapping:** An upload subsystem with a **StorageProvider SPI** (CDI `@Inject Instance<StorageProvider>`); default local provider + S3 provider via the Quarkus Amazon S3 extension. Image processing via a Java lib (e.g. imgscalr/thumbnailator) guarded for native image. Assets are **tenant-scoped and RLS-aware** like any other content.

### 3.5 Internationalization (i18n)
- Per-entry locales; create/manage translations; `locale` API param; configurable available locales; per-field localization toggles.

> **Quarkus mapping:** `locale` column on entries + a localization-group id linking translations; Document Service resolves locale. Field-level localization driven by `@Field(localized = true)`.

### 3.6 Access control & auth — **roles + row-level security + tenant-aware**
- **Admin RBAC:** granular roles/permissions for *admin panel* users (who can edit which content types/fields/actions).
- **Users & Permissions:** end-user auth for the *content API* (register/login, JWT, roles like `authenticated`/`public`, per-route permissions, provider logins).
- API tokens for server-to-server access.

> **Quarkus mapping:** Quarkus Security as the backbone, with **three layered enforcement points**:
> 1. **RBAC (coarse, action-level):** can this identity call this operation on this content type/field? Stored in DB, enforced via `@RolesAllowed` + a CDI security augmentor and dynamic policy checks.
> 2. **Row-Level Security (fine, row-level):** of the rows this action could touch, which may *this* identity actually see/modify? Enforced by the Hibernate-filter/Panache mechanism in 3.3.
> 3. **Tenant isolation (hard boundary):** every query is additionally constrained to the resolved tenant (3.10), so RLS operates *within* a tenant and cross-tenant access is structurally impossible.
> Two identity realms (admin users; API consumers + API tokens via SmallRye JWT). RLS predicates can reference both identity (`:currentUserId`, roles) and tenant (`:currentTenant`).

### 3.7 Extensibility
- **Plugin system:** official/community plugins and custom ones; lifecycle hooks; admin-panel injection zones.
- **Lifecycle hooks / middleware:** `beforeCreate`, `afterUpdate`, etc.; custom controllers/services/routes; policies & middlewares.
- **Webhooks:** fire HTTP callbacks on content events with payload + headers + retry.

> **Quarkus mapping:** Plugins = additional Quarkus extensions or CDI modules contributing build items / observing CDI events. Lifecycle hooks = **CDI events** (`@Observes EntryCreated`) the Document Service fires — these are also what **workflow transitions (3.11)** and **webhooks** subscribe to. Webhooks = a delivery service with persistence + retry (Quarkus Scheduler / Messaging).

### 3.8 Developer & ops experience
- TypeScript-first, Vite-fast admin builds, CLI scaffolding & upgrade codemons, plugin SDK, OpenAPI/GraphQL playground, multi-environment config.

> **Quarkus mapping:** Lean on Quarkus' own DX — `quarkus:dev` live reload, Dev UI, Dev Services, config profiles, Maven/Gradle plugin, Quarkus CLI. Provide a `quarkus-cms` Dev UI card (type browser, seed admin/tenant, open admin panel) and code-gen Maven goals (e.g. `quarkus-cms:new-type`).

### 3.10 Multi-tenancy (new — first-class deployment mode)
- **Goal:** one deployment serves many isolated tenants (organizations/sites), each with its own content, media, users, roles, and (optionally) schema — with no data bleed between tenants.
- **Strategies (configurable, `quarkus.cms.tenancy.strategy`):**
  - `DISCRIMINATOR` (default) — shared schema, `tenant_id` column on every table; cheapest, pairs naturally with RLS.
  - `SCHEMA` — one Postgres schema per tenant; stronger isolation.
  - `DATABASE` — one datasource per tenant; strongest isolation, heaviest ops.
- **Tenant resolution:** pluggable `TenantResolver` SPI — resolve from a JWT claim, an `X-Tenant` header, subdomain, or path prefix.
- **Tenant-aware everything:** Document Service, media, RBAC, RLS, workflow, webhooks, and the admin panel all operate within the resolved tenant. Cross-tenant access requires an explicit platform-admin scope.

> **Quarkus mapping:** Built on **Hibernate ORM multitenancy** (`quarkus.hibernate-orm.multitenant`) with a CDI `TenantResolver`. For `DISCRIMINATOR`, the `tenant_id` predicate is injected by the same Hibernate-filter layer as RLS so the two compose cleanly. Dev Services seeds two demo tenants to make isolation testable from day one.

### 3.11 Basic workflow engine (new — review workflows)
- **Goal:** match Strapi's (Enterprise-only) **Review Workflows**: configurable stages an entry moves through before publication, with role-gated transitions and assignees.
- **Model:**
  - `WorkflowDefinition` per content type (or shared): an ordered set of **stages** (e.g. `Draft → In Review → Ready → Published`) and the **allowed transitions** between them, each transition gated by required role/permission.
  - `WorkflowState` on each entry: current stage, optional assignee, timestamps, optional comment/note on transition.
  - A `WorkflowService` enforces that only legal transitions occur and only by permitted roles, integrating with draft/publish (reaching the terminal stage can trigger publish).
- **Keep it basic:** a role-gated **finite state machine**, not BPMN. No parallel gateways, timers, or sub-processes in v1.

> **Quarkus mapping:** A small FSM (`WorkflowService` CDI bean) over stage/transition metadata in DB. Transitions fire **CDI events** (`EntryStageChanged`) that webhooks and notifications subscribe to (reuses 3.7's event bus). Workflow is **tenant-scoped and RLS-aware**, and shows up in the Content Manager as a stage selector + history. Note as a future option: swap the built-in FSM for an embedded engine (Kogito/Flowable) behind the same `WorkflowService` interface if heavier orchestration is later needed.

### 3.9 What we explicitly defer / exclude (v1)
- Strapi AI content-type generation, Strapi Cloud hosting, the visual page-builder marketplace, and Figma import. Advanced BPMN-style workflow orchestration is also out of scope (basic FSM only). Note them as future ideas only.

---

## 4. Target Architecture (Quarkus-native)

```
┌──────────────────────────────────────────────────────────────┐
│  Admin SPA (React/Lit static assets served by the extension)  │
│  /cms-admin → Content-Type Builder (codegen) · Content Mgr ·  │
│              Media · Roles & Row Policies · Workflow · Tenants │
└───────────────┬──────────────────────────────────────────────┘
                │ Admin REST + Auth (admin RBAC realm)
┌───────────────▼──────────────────────────────────────────────┐
│                    quarkus-cms RUNTIME module                  │
│                                                                │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌─────────────┐ │
│  │ REST API  │  │ GraphQL   │  │ Admin API │  │ Media API   │ │
│  │ adapter   │  │ adapter   │  │ (CTB,CM)  │  │ (upload)    │ │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └──────┬──────┘ │
│        └──────────────┴──────┬───────┴───────────────┘        │
│                    ┌─────────▼──────────┐                     │
│                    │  DOCUMENT SERVICE  │  (CRUD, query,      │
│                    │  (CDI bean)        │   populate, draft/  │
│                    └─────────┬──────────┘   publish, locale)  │
│        ┌──────────────┬──────┼───────────┬──────────────┐     │
│  ┌─────▼─────┐ ┌──────▼────┐ │   ┌────────▼───────┐ ┌────▼───┐│
│  │ Type      │ │ Security: │ │   │ Tenant         │ │Workflow││
│  │ Registry  │ │ RBAC +    │ │   │ Resolver +     │ │ FSM    ││
│  │ (Jandex,  │ │ Row-Level │ │   │ multitenancy   │ │service ││
│  │ build-    │ │ Security  │ │   │ (DISCRIM/      │ │        ││
│  │ time)     │ │ policies  │ │   │  SCHEMA/DB)    │ │        ││
│  └───────────┘ └─────┬─────┘ │   └────────┬───────┘ └────┬───┘│
│                      │  ┌────▼────────────▼──────┐       │    │
│                      └─►│ Hibernate filters (RLS  │◄──────┘    │
│                         │ + tenant) → ORM/Panache │            │
│                         │ + Flyway + (opt) PG RLS │            │
│                         └────────────┬────────────┘            │
│                 ┌────────────────────┤                         │
│                 │ Event bus (CDI): EntryCreated/Updated/       │
│                 │ Published/StageChanged → webhooks + notify   │
└─────────────────┴────────────────────┼─────────────────────────┘
                            ┌───────────▼────────────┐
                            │ PostgreSQL / MySQL      │ (Dev Services)
                            │ (per-tenant: row/schema/db)
                            └─────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│  quarkus-cms DEPLOYMENT module (@BuildStep / @Recorder)        │
│  · discover @ContentType classes (Jandex) → entities + routes  │
│  · register reflection for native · Dev UI · Dev Services       │
│  · generate Java source from Content-Type Builder edits         │
└──────────────────────────────────────────────────────────────┘
```

**Module layout (Quarkiverse convention):**
```
quarkus-cms-parent
├── runtime/      # @ConfigMapping, annotations, Document Service, RLS, tenancy, workflow, adapters, SPIs
├── deployment/   # @BuildStep (type discovery, codegen), @Recorder, Dev UI, Dev Services, native config
├── admin-ui/     # SPA build (Vite), output bundled into runtime resources
├── integration-tests/  # @QuarkusTest + @QuarkusIntegrationTest (native) incl. RLS/tenant negative tests
└── docs/         # antora/markdown docs
```

---

## 5. Phased Plan (milestones with acceptance criteria)

> Each phase is independently demoable. Hermes completes acceptance criteria before moving on.

### Phase 0 — Spike & decide (foundations)
**Build:** Quarkiverse extension skeleton (runtime+deployment), CI (JVM+native), Dev Services for Postgres. Design and prototype the **`@ContentType` annotation model** and **build-time discovery** (Jandex `@BuildStep`) that turns an annotated Java class into a Panache entity + a dynamic `/api/{plural}` route. Decide how the Content-Type Builder will generate Java source.
**Acceptance:**
- `quarkus ext add` works; `quarkus:dev` boots with auto-started Postgres.
- ADR in `DECISIONS.md` fixing the annotation API and the code-first-vs-overlay stance (default: code-first; overlay = optional escape hatch).
- One `@ContentType`-annotated `Article` class is discovered at build time and is queryable over `/api/articles`, in JVM **and** native.

### Phase 1 — Type Registry + Document Service + REST (the core)
**Build:** Build-time type discovery for `@ContentType` classes (collection + single), fields, relations, components, dynamic zones → Panache entities + Flyway-managed schema. Document Service CRUD. Dynamic REST adapter implementing Strapi-compatible query params (`filters`, `sort`, `pagination`, `fields`, `populate`) and `{data,meta}` envelope.
**Acceptance:**
- Adding a `@ContentType` class → REST CRUD works end-to-end, native included.
- All five query features pass integration tests; OpenAPI doc auto-generated.
- Relations (1-1, 1-n, n-n) and Components/Dynamic Zones persist & populate correctly.

### Phase 2 — Admin panel: Content-Type Builder (codegen) + Content Manager
**Build:** Admin SPA at `/cms-admin`. Content-Type Builder UI that **generates/edits Java source files** under the configured package (fields, relations, components, dynamic zones) and triggers live reload. Content Manager UI (auto-generated list + edit forms per field type). Admin API + admin auth.
**Acceptance:**
- A user defines a new type in the UI → a Java class is written → its REST API is live after reload, with zero handwritten controller code.
- Dynamic Zones supported in model + editor.
- Generated source is clean, compiles, and round-trips (UI ⇄ file).

### Phase 3 — Auth, RBAC, Row-Level Security & Multi-tenancy
**Build:** Admin RBAC (roles/permissions over content types, fields, actions). Users-&-Permissions for the content API (register/login, JWT, `public`/`authenticated`, per-route permissions, API tokens). **Row-Level Security**: `@RowPolicy` + admin-managed row policies enforced via Hibernate filters/Panache augmentation, with optional Postgres native RLS. **Multi-tenancy**: Hibernate ORM multitenancy (DISCRIMINATOR default) + `TenantResolver` SPI; tenant-scoped content/media/users/policies.
**Acceptance:**
- RBAC enforced on admin + content APIs (allow/deny tests).
- A user cannot read or modify another user's rows (RLS negative tests); `bypassRowPolicy` works for editors.
- Cross-tenant access is structurally impossible (cross-tenant leakage tests for API, media, admin); tenant resolved from JWT/header; native verified.

### Phase 4 — GraphQL API
**Build:** SmallRye GraphQL adapter generating schema from the type registry: queries, mutations, filtering/pagination/populate parity with REST, GraphQL playground — all routed through the Document Service so RLS + tenant filters apply identically.
**Acceptance:**
- Every content type auto-exposed in GraphQL; CRUD + nested populate tested.
- RLS + tenant isolation hold over GraphQL (same negative tests pass).

### Phase 5 — Media Library
**Build:** Upload subsystem, StorageProvider SPI (local + S3), folders, metadata, image thumbnail/variant generation, `@Media` field type wired into builder/manager/APIs. Assets tenant-scoped + RLS-aware.
**Acceptance:**
- Upload, list, search, delete assets via API and admin UI; relate media to entries.
- S3 provider configurable; thumbnails generated; native-safe image processing; tenant isolation verified.

### Phase 6 — Draft/Publish, History, i18n & Workflow engine
**Build:** Draft+published variants with publish action and two-tab editor; append-only version history with restore; i18n (locales, translation groups, `locale` param, per-field localization). **Basic workflow engine**: `WorkflowDefinition` (stages + role-gated transitions), `WorkflowState` per entry, `WorkflowService` FSM firing `EntryStageChanged` CDI events; reaching the terminal stage can trigger publish; stage selector + history in the Content Manager.
**Acceptance:**
- Draft→publish lifecycle exposed via `status` param; restore from history; localized entries served by locale.
- Entries move only through legal, role-permitted stage transitions; illegal transitions rejected with tests; workflow is tenant-scoped and RLS-aware.

### Phase 7 — Extensibility: lifecycle hooks, webhooks, plugin SPI
**Build:** CDI lifecycle events (`EntryCreated/Updated/Deleted/Published/StageChanged`); webhook delivery service (subscriptions, payloads, signing, retry) driven by those events; plugin SPI + a sample plugin; admin-panel injection points; Dev UI card.
**Acceptance:**
- A custom CDI observer hooks entry/stage lifecycle; webhook fires on publish/stage-change with retry.
- A sample plugin adds a field type or route purely through the SPI.

### Phase 8 — Hardening, docs, native & release
**Build:** Full native verification across the matrix, performance/memory benchmarks vs targets, security review (incl. RLS + tenant boundary audit), OpenAPI/GraphQL docs, getting-started guide, multi-tenant example app, semantic-versioned `1.0.0` release to Quarkiverse/Maven Central.
**Acceptance:**
- Green CI on JVM + native; documented quickstart reproduces the Section 1 demo verbatim.
- Security/tenant-isolation audit checklist passes; published artifact installable via `quarkus ext add`.

---

## 6. Recommended Tech Stack (defaults Hermes should use)

| Concern | Choice |
|---|---|
| Extension model | Quarkiverse parent, runtime + deployment modules |
| Content modeling | **Code-first: `@ContentType` Java classes discovered at build time (Jandex)** |
| Persistence | Hibernate ORM with Panache (+ Reactive where feasible), Flyway migrations |
| DB | PostgreSQL (primary), MySQL/MariaDB, H2 (dev) |
| REST | Quarkus REST (RESTEasy Reactive) + Jackson + SmallRye OpenAPI |
| GraphQL | SmallRye GraphQL |
| AuthN/Z | Quarkus Security + SmallRye JWT + Elytron; API tokens |
| Row-Level Security | Hibernate filters (`@FilterDef`/`@Filter`) + Panache query augmentation; optional PostgreSQL native RLS policies |
| Multi-tenancy | Hibernate ORM multitenancy (DISCRIMINATOR default / SCHEMA / DATABASE) + CDI `TenantResolver` SPI |
| Workflow | Built-in role-gated FSM (`WorkflowService`) + CDI events; pluggable for Kogito/Flowable later |
| Validation | Hibernate Validator |
| Media storage | StorageProvider SPI; local + Quarkus Amazon S3 |
| Events/webhooks | CDI events + Quarkus Scheduler / Messaging for retry |
| Dev experience | Dev Services (Postgres + seeded tenants), Dev UI card, live reload |
| Admin SPA | Vite + React (or Lit), bundled as static resources |
| Testing | JUnit 5, `@QuarkusTest`, `@QuarkusIntegrationTest`, Testcontainers; RLS + cross-tenant negative tests |
| Native | GraalVM/Mandrel; reflection/serialization registered via build items |

---

## 7. Definition of Done (whole project)

The project is done when the **Section 1 demo runs exactly as written** on a clean machine, in both JVM and native modes, with: code-first content types via annotated Java classes (and a UI that generates them), auto REST + GraphQL, media uploads, RBAC + JWT auth, **row-level security**, **multi-tenant isolation**, draft/publish + i18n + history, a **basic review-workflow engine**, webhooks + plugin SPI — all covered by an automated test suite (including security, RLS, and cross-tenant negative tests) and published as an installable Quarkus extension with a quickstart guide.

---

## 8. Key Risks & Mitigations (for Hermes to watch)

- **Code-first modeling vs. Strapi's runtime UI builder.** The Content-Type Builder must generate clean Java source and live-reload reliably; if codegen is brittle, the "no-code modeling" UX suffers. Mitigate: make the file the source of truth, keep codegen idempotent and round-trippable, lean on `quarkus:dev` reload. Offer the runtime overlay only as a documented escape hatch.
- **RLS correctness & performance.** Row filters must be applied on *every* path (REST, GraphQL, admin, exports) with no bypass, and must not tank query plans. Mitigate: centralize all data access in the Document Service, enable Hibernate filters from the security identity in one place, add negative + load tests, consider Postgres RLS as a backstop.
- **Tenant leakage.** The worst failure mode. Mitigate: tenant predicate injected by the same single filter layer as RLS, default-deny, mandatory cross-tenant negative tests in CI, seed two tenants in Dev Services so isolation is exercised constantly.
- **Native image + reflection** on dynamic JSON/entities — register everything via build items; add native ITs early, not at the end.
- **Workflow scope creep.** Keep it a role-gated FSM; resist BPMN features. Hide heavier orchestration behind the `WorkflowService` interface for a later swap.
- **Scope creep toward "full Strapi."** Hold the phase gates; defer AI/Cloud/page-builder.

---

## Sources (Strapi feature analysis)
- [Content-Type Builder — Strapi 5 Docs](https://docs.strapi.io/cms/features/content-type-builder)
- [Documents / Document Service API — Strapi 5 Docs](https://docs.strapi.io/cms/api/document)
- [Content API — Strapi 5 Docs](https://docs.strapi.io/cms/api/content-api)
- [REST API reference — Strapi 5 Docs](https://docs.strapi.io/cms/api/rest)
- [Media Library — Strapi 5 Docs](https://docs.strapi.io/cms/features/media-library)
- [Developer experience improvements in Strapi 5](https://strapi.io/blog/developer-experience-improvements-strapi-5)
- [Strapi adoption guide — LogRocket](https://blog.logrocket.com/strapi-adoption-guide/)
- [Strapi — Jamstack headless CMS directory](https://jamstack.org/headless-cms/strapi/)
