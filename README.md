# Quarkus CMS

A **Strapi-inspired headless CMS, delivered as a Quarkus extension** — with the
enterprise concerns Strapi charges for (row-level security, multi-tenancy, review
workflows) built in. Model content as **type-safe Java classes**, get an
auto-generated and secured REST + GraphQL API instantly, manage entries and media,
and isolate everything per tenant — all cloud-native, fast to start, low memory,
GraalVM-native-image compatible.

> Status: **experimental / Phase 0 scaffold.** Starting point for the build described
> in `docs/HERMES_BRIEF.md` (Revision 2). The current code is a working vertical slice
> plus the contracts for the Revision-2 capabilities.

## Code-first modeling

Content types are **annotated Java classes** under a configured package — the single
source of truth, discovered at build time (no `schema.json`). See
[`docs/examples/Article.java`](docs/examples/Article.java):

```java
@ContentType(api = "article", plural = "articles", draftAndPublish = true)
@TenantScoped
@RowPolicy(name = "own-articles", expression = "author.id = :currentUserId", roles = "author")
public class Article {
    @Field(required = true) public String title;
    @Field(localized = true) public String body;
    @Relation(Relation.Kind.MANY_TO_ONE) public Author author;
}
```

The admin Content-Type Builder (Phase 2) *generates* these classes and triggers live
reload; the file stays authoritative.

## Module layout

```
quarkus-cms-parent
├── runtime/             # annotations, model, DocumentService, RLS, tenancy, workflow, REST
├── deployment/          # @BuildStep wiring: @ContentType discovery, beans, reflection, Dev stubs
├── integration-tests/   # @QuarkusTest (JVM) + native verification profile
└── docs/                # the Hermes brief + architecture decisions + example type
```

## What already works / is contracted (Phase 0)

- Proper Quarkus extension: `quarkus-cms` (runtime) + `quarkus-cms-deployment`.
- Code-first annotation model + build-time `@ContentType` discovery (Jandex) stub.
- A single templated JAX-RS resource (`/api/{plural}`) dispatching to any registered
  type, returning the Strapi-style `{ data, meta }` envelope.
- In-memory reference `DocumentService` (CRUD + draft/publish).
- SPIs/contracts for the Revision-2 features: `RowPolicyEnforcer` + `SecurityContext`
  (RLS), `TenantResolver` + `TenancyStrategy` (multi-tenancy), `WorkflowService` +
  `WorkflowDefinition` (basic FSM workflow).
- Build-time CDI bean + native-reflection registration.
- Integration tests proving the dynamic API end-to-end.

## Build & test

```bash
./mvnw verify              # JVM build + tests   (needs JDK 21 + Maven; see MAVEN_WRAPPER_NOTE.md)
./mvnw verify -Dnative     # native verification of the integration-tests module
```

## Next steps

Follow the phased plan in [`docs/HERMES_BRIEF.md`](docs/HERMES_BRIEF.md) and the decision
records in [`DECISIONS.md`](DECISIONS.md). Immediate Phase 0 → Phase 1 work: turn a
discovered `@ContentType` class into a Panache entity (replacing `InMemoryDocumentService`)
and wire the `RowPolicyEnforcer` + `TenantResolver` into every query.
