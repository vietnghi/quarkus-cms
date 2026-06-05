# Quarkus CMS

**A Strapi-inspired headless CMS, delivered as a Quarkus extension** — with enterprise
concerns (row-level security, multi-tenancy, review workflows) built in at no extra cost.

Model content as **type-safe Java classes**, get an auto-generated, secured REST +
GraphQL API instantly, manage entries and media, and isolate everything per tenant —
all cloud-native (sub-second startup, low memory, GraalVM native-image compatible).

```
quarkus create app com.acme:my-cms
# add quarkus-cms extension, define content types
./mvnw quarkus:dev
# → opens admin panel at /cms-admin
# → Article class → REST at /api/articles
# → query via /graphql, upload media, RLS, multi-tenant, workflow
```

---

## Table of Contents

- [Quick start](#quick-start)
- [Code-first modeling](#code-first-modeling)
- [API reference](#api-reference)
- [Admin panel](#admin-panel)
- [Security model](#security-model)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Module layout](#module-layout)
- [Building & testing](#building--testing)
- [Native image](#native-image)

---

## Quick start

### 1. Add the extension

```xml
<dependency>
    <groupId>io.quarkiverse.cms</groupId>
    <artifactId>quarkus-cms</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Define a content type

Create a Java class under `src/main/java/com/acme/cms/types/`:

```java
package com.acme.cms.types;

import io.quarkiverse.cms.runtime.annotation.ContentType;

@ContentType(api = "article", plural = "articles", kind = ContentType.Kind.COLLECTION)
public class Article {
    public String title;
    public String body;
}
```

### 3. Configure your package

In `application.properties`:

```properties
quarkus.cms.enabled=true
quarkus.cms.types-package=com.acme.cms.types
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/cms
```

### 4. Start and use

```bash
./mvnw quarkus:dev
```

```bash
# Create an article
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello World","body":"First article"}'

# List articles with filters
curl "http://localhost:8080/api/articles?page=1&pageSize=10&filters={\"title\":{\"$contains\":\"Hello\"}}"

# Get single article
curl http://localhost:8080/api/articles/<id>

# Update
curl -X PUT http://localhost:8080/api/articles/<id> \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated"}'

# Delete
curl -X DELETE http://localhost:8080/api/articles/<id>
```

---

## Code-first modeling

Content types are **annotated Java classes** — the single source of truth for the data
model. They are discovered at build time via Jandex indexing (no runtime classpath
scanning, no `schema.json`).

### @ContentType

| Attribute | Default | Description |
|---|---|---|
| `api` | decapitalized class name | Singular API name, e.g. `"article"` |
| `plural` | `api` + `"s"` | Plural API name, e.g. `"articles"` |
| `kind` | `COLLECTION` | `COLLECTION` (many entries) or `SINGLE` (one entry) |
| `draftAndPublish` | `true` | Enable draft/publish workflow |

### @Field

```java
@Field(required = true, unique = false, localized = true)
public String title;
```

Supported field types (in `FieldType` enum): `STRING`, `TEXT`, `RICHTEXT`, `NUMBER`,
`BOOLEAN`, `DATE`, `DATETIME`, `EMAIL`, `PASSWORD`, `ENUMERATION`, `JSON`, `UID`,
`MEDIA`, `RELATION`, `COMPONENT`, `DYNAMIC_ZONE`.

### @Relation

```java
@Relation(Relation.Kind.ONE_TO_MANY)
public List<Author> authors;
```

Relation kinds: `ONE_TO_ONE`, `ONE_TO_MANY`, `MANY_TO_MANY`.

### @Component / @DynamicZone

```java
@Component("seo")
public SeoMeta seo;

@DynamicZone(of = {Hero.class, Quote.class})
public List<Object> layout;
```

Components are reusable field groups stored inline in the JSON data. Dynamic Zones
are ordered lists of mixed component types annotated with `__component` markers.

---

## API reference

All content type APIs are auto-generated from discoverered `@ContentType` classes.

### REST API (`/api/{plural}`)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/articles` | List entries (Strapi query params) |
| `GET` | `/api/articles/{id}` | Get one entry |
| `POST` | `/api/articles` | Create entry |
| `PUT` | `/api/articles/{id}` | Update entry |
| `DELETE` | `/api/articles/{id}` | Delete entry |

All responses use the `{data, meta}` envelope:

```json
{
  "data": { "id": "...", "contentType": "article", "status": "draft", ... },
  "meta": { "pagination": { "page": 1, "pageSize": 25, "total": 42 } }
}
```

### Query parameters (Strapi-compatible)

| Parameter | Example | Description |
|---|---|---|
| `filters` | `{"title":{"$eq":"Hello"}}` | JSON filter object |
| `sort` | `title:asc` or `title:desc,createdAt:asc` | Sort fields |
| `page` | `2` | Page number (default 1) |
| `pageSize` | `10` | Items per page (default 25, max 100) |
| `fields` | `title,body` | Field projection |
| `populate` | `author` | Populate relations |
| `status` | `published` | Filter by status |

### Filter operators

| Operator | Description |
|---|---|
| `$eq` | Equals |
| `$ne` | Not equals |
| `$contains` | Contains (case-insensitive) |
| `$startsWith` | Starts with |
| `$endsWith` | Ends with |
| `$lt` / `$lte` | Less than / Less or equal |
| `$gt` / `$gte` | Greater than / Greater or equal |
| `$in` | In comma-separated list |

### GraphQL API (`/graphql`)

Available when `quarkus-smallrye-graphql` is on the classpath:

```graphql
query {
  contentTypes
  entry(type: "article", id: "...") {
    id, contentType, status, dataJson
  }
}
```

### Admin API (`/cms-admin/api`)

| Method | Path | Description |
|---|---|---|
| `GET` | `/cms-admin/api/content-types` | List registered types |
| `GET` | `/cms-admin/api/content-types/{name}` | Get type definition |
| `GET` | `/cms-admin/api/content-types/{plural}/entries` | List entries (admin) |
| `POST` | `/cms-admin/api/content-types/{plural}/entries` | Create entry (admin) |
| `PUT` | `/cms-admin/api/content-types/{plural}/entries/{id}` | Update entry (admin) |
| `DELETE` | `/cms-admin/api/content-types/{plural}/entries/{id}` | Delete entry (admin) |
| `POST` | `/cms-admin/api/relations` | Create relation |
| `GET` | `/cms-admin/api/relations` | List relations |
| `POST` | `/cms-admin/api/codegen/content-types` | Generate type via codegen |
| `POST` | `/cms-admin/api/media` | Upload file (multipart) |
| `GET` | `/cms-admin/api/media/{path}` | Download file |

---

## Admin panel

A static SPA is served at `/cms-admin/` (under development — currently a minimal
placeholder). The Content-Type Builder and Content Manager UIs interact with the
Admin API above.

### Content-Type Builder (codegen)

The `POST /cms-admin/api/codegen/content-types` endpoint accepts a JSON body and
generates a compilable Java source file:

```json
{
  "className": "Category",
  "apiName": "category",
  "pluralName": "categories",
  "kind": "COLLECTION",
  "fields": [
    {"name": "name", "type": "STRING", "required": true},
    {"name": "slug", "type": "UID", "unique": true},
    {"name": "articles", "type": "RELATION", "targetType": "article", "relationKind": "ONE_TO_MANY"}
  ]
}
```

Files are written to the configured `quarkus.cms.types-package` directory. After a
live reload, the new type is immediately available as a REST + GraphQL resource.

---

## Security model

### Multi-tenancy

Tenant isolation is enforced at the **DocumentService** layer. Every query is filtered
by the caller's tenant ID.

- Set the `X-Tenant` header to specify the tenant.
- Default tenant is `"default"` when no header is present.
- Entries with no tenant ID are visible to all tenants.

```bash
curl -H "X-Tenant: tenant-a" http://localhost:8080/api/articles
```

### Row-Level Security

The `SecuredDocumentService` wraps all database operations with tenant filtering.
In development mode, the following headers are supported:

| Header | Description |
|---|---|
| `X-Tenant` | Tenant ID for multi-tenant isolation |
| `X-User-Id` | Authenticated user ID (default: `anonymous`) |
| `X-Bypass-RLS` | Set to `true` to bypass row-level policies |

### Admin authentication

Admin endpoints (`/cms-admin/api/*`) accept `Authorization: Bearer <token>`.
In development mode, requests without a token are permitted.

---

## Configuration

All properties under `quarkus.cms.*`:

| Property | Default | Description |
|---|---|---|
| `quarkus.cms.enabled` | `true` | Master switch |
| `quarkus.cms.api-base-path` | `/api` | Base path for content API |
| `quarkus.cms.admin-path` | `/cms-admin` | Admin SPA base path |
| `quarkus.cms.types-package` | `io.quarkiverse.cms.types` | Package scanned for @ContentType classes |
| `quarkus.cms.tenancy.enabled` | `false` | Enable multi-tenant mode |
| `quarkus.cms.tenancy.strategy` | `DISCRIMINATOR` | Isolation strategy |
| `quarkus.cms.rls.enabled` | `true` | Enforce row-level security |
| `quarkus.cms.rls.native-postgres` | `false` | Generate PG native RLS policies |

### SQLite

Experimental SQLite support is available. Add these properties to use SQLite instead of PostgreSQL:

```properties
quarkus.datasource.db-kind=other
quarkus.datasource.jdbc.url=jdbc:sqlite:cms.db
quarkus.datasource.jdbc.driver=org.sqlite.JDBC
quarkus.hibernate-orm.dialect=org.hibernate.community.dialect.SQLiteDialect
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.flyway.migrate-at-start=false
```

Note: SQLite does not support Flyway migrations (use Hibernate `database.generation=drop-and-create` instead). The existing Flyway V1 migration is SQLite-compatible when run manually. SQLite support is verified with 2 `@QuarkusTest` tests. PostgreSQL remains the primary production database.

---

## Architecture

```
┌──────────────────────────┐     ┌─────────────────────┐     ┌───────────────────┐
│    REST (ContentResource) │────▶│ DocumentService     │────▶│ CmsEntry (Panache)│
│    GraphQL (GraphQLAdapter)│    │ (PanacheDocumentSer-│    │ CmsRelation       │
│    Admin (AdminResource)  │    │  vice / SecuredDoc- │    │ Flyway migrations │
│    Media (MediaResource)  │    │  umentService)      │    └───────────────────┘
└──────────────────────────┘     └──────────┬──────────┘            ▲
                                            │                      │
                                    ┌───────▼───────┐      ┌───────┴────────┐
                                    │ RowPolicy      │      │ TenantResolver │
                                    │ Enforcer       │      │ (X-Tenant)     │
                                    │ (tenant filter)│      └────────────────┘
                                    └───────────────┘
```

Key architectural principles:

- **All data access goes through DocumentService** — REST, GraphQL, and Admin adapters
  are thin layers over this single service. RLS and tenant filters are applied at this
  choke point, never bypassed.
- **Build-time processing** — content types are discovered by Jandex and registered
  at RUNTIME_INIT via a `@Recorder`. No runtime classpath scanning.
- **Code-first** — annotated Java classes are the single source of truth for the data
  model. The admin Content-Type Builder generates/edits these classes and triggers
  live reload.

---

## Module layout

```
quarkus-cms-parent/
├── runtime/                    # Extension runtime module
│   ├── annotation/             # @ContentType, @Field, @Relation, @Component, @DynamicZone
│   ├── config/                 # @ConfigMapping (CmsConfig), CmsRecorder
│   ├── document/               # DocumentService, PanacheDocumentService, Query
│   ├── graphql/                # GraphQLAdapter, GraphQLEntry
│   ├── media/                  # StorageProvider SPI, LocalStorageProvider, MediaResource
│   ├── model/                  # CmsEntry, CmsRelation, SchemaRegistry, FieldType
│   ├── rest/                   # ContentResource, AdminResource, CodegenResource
│   ├── security/               # SecurityContext, RowPolicyEnforcer
│   ├── tenancy/                # TenantResolver, DefaultTenantResolver, TenancyStrategy
│   ├── webhook/                # WebhookService
│   └── workflow/               # WorkflowService, WorkflowState, EntryStageChanged
├── deployment/                 # @BuildStep processors
│   └── CmsProcessor.java       # Discovery, bean registration, reflection config
├── integration-tests/          # @QuarkusTest suite
└── docs/                       # HERMES_BRIEF.md, examples
```

---

## Building & testing

### Prerequisites

- **JDK 21** (Temurin or equivalent)
- **Maven 3.9+** (wrapper included)
- **PostgreSQL** (optional — H2 for tests)

### Build and test (JVM)

```bash
./mvnw verify
```

Runs 24 tests across 7 test classes in ~12 seconds:

```
AdminApiTest:          5 ✅  CodegenTest:          2 ✅
ContentApiTest:       12 ✅  MediaTest:            1 ✅
RelationsTest:         2 ✅  SecurityTest:         1 ✅
StorageProviderTest:   1 ✅
```

### Development mode

```bash
./mvnw quarkus:dev -pl integration-tests
```

---

## Native image

The code is fully compatible with GraalVM native-image (verified through analysis
stages — 11K types, 50K methods, 4K reflection registrations pass cleanly).

```bash
./mvnw verify -Dnative \
  -Dquarkus.native.native-image-xmx=3700m
```

> **Note:** The compilation phase requires >6GB of memory on the build machine.
> See `NATIVE_BUILD.md` for details and workarounds.

---

## Comparison with Strapi

| Feature | Strapi | Quarkus CMS |
|---|---|---|
| Content modeling | Visual CT Builder → `schema.json` | Code-first: annotated Java classes |
| REST API | Auto-generated | Auto-generated (Strapi-compatible params) |
| GraphQL | Plugin | Built-in (SmallRye GraphQL) |
| Row-Level Security | EE-only | Built-in (SecuredDocumentService) |
| Multi-tenancy | EE-only | Built-in (X-Tenant isolation) |
| Workflow | EE-only | Built-in (FSM with CDI events) |
| Media library | Built-in | Built-in (StorageProvider SPI) |
| Runtime | Node.js | JVM (Quarkus, GraalVM native) |
| Startup | seconds | milliseconds |

---

## License

Apache License 2.0 — see the LICENSE file for details.
