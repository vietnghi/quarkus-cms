# Hermes Agent — Goal-Mode Instructions for `quarkus-cms`

Tailored to **Hermes Agent** (Nous Research) `/goal` mode — the Ralph-loop where, after
every turn, a judge model reads your **last response** and decides `done` or `continue`,
with a **20-turn budget** before it auto-pauses. (Docs: `/docs/user-guide/features/goals`.)

### Why this matters for how we drive it
1. **One `/goal` per phase, not one for the whole project.** The build is 9 phases (0–8);
   each is far more than a 20-turn loop and has its own gate. Set a goal per phase, let it
   run, `/goal resume` when the budget hits, then move to the next phase.
2. **The judge only sees your last ~4 KB of text** and is deliberately conservative — it
   marks `done` only when the response *explicitly* confirms completion. So every goal here
   forces Hermes to **end each turn with a fixed status line** (`GOAL-COMPLETE:` /
   `GOAL-INCOMPLETE:`) and to **show the build output**. That's what makes the judge correct.
3. **Acceptance criteria must be machine-checkable.** Each goal embeds the phase's criteria
   plus the literal commands that prove them (`./mvnw verify`, `./mvnw verify -Dnative`).

Replace `REPO` below with the absolute path where you unzip the scaffold (e.g. `~/quarkus-cms`).

---

## STEP 0 — One-time setup

**0a. Config** — in `~/.hermes/config.yaml` raise the budget for build-heavy phases and route
the judge to a cheap fast model:
```yaml
goals:
  max_turns: 40          # phases 1–6 are long; you'll still /goal resume sometimes
auxiliary:
  goal_judge:
    provider: openrouter
    model: google/gemini-3-flash-preview   # small, ~200-token judge calls
```

**0b. Get the project into Hermes' workspace** — send as a normal message (not a `/goal`):
```
Unzip quarkus-cms-scaffold.zip into REPO. Then read these in full and don't start coding yet:
REPO/docs/HERMES_BRIEF.md (the spec + 9-phase plan with per-phase Acceptance Criteria),
REPO/docs/HERMES_PROMPT.md (this file), and REPO/DECISIONS.md (accepted ADRs 0001–0006).
Confirm your toolchain: Java 21 and Maven available, Docker available for Testcontainers/Dev
Services. Reply with: the phase you believe is next (should be Phase 1), a one-paragraph plan,
and confirmation the scaffold builds via `./mvnw verify`. Fix the build if it's red.
```

---

## STEP 1 — Prime the session (normal message, paste once)

Send this as a normal message **before** the first `/goal`. It sets the standing rules the
continuation loop will carry through every turn:
```
You are building the quarkus-cms Quarkus extension. Operate by these rules for the whole project:

SOURCES OF TRUTH: REPO/docs/HERMES_BRIEF.md (spec, architecture, Section 5 = the 9-phase plan
with Acceptance Criteria) and REPO/DECISIONS.md (ADR-0001..0006 — accepted; don't re-litigate,
supersede with a new ADR if you must deviate).

NON-NEGOTIABLE CONSTRAINTS:
- Proper Quarkus extension (runtime + deployment, @BuildStep/@Recorder/BuildItems). Extend the
  scaffold; don't rewrite it.
- JVM AND GraalVM native must both build every phase. Register reflection/serialization at build time.
- Code-first modeling: @ContentType Java classes discovered at build time are the ONLY model
  source (no schema.json). The Content-Type Builder UI generates Java source.
- ALL data access goes through the DocumentService; RLS + tenant filters apply there and are
  never bypassed by any adapter (REST/GraphQL/admin/export).
- Every endpoint and every security/tenant boundary has tests, including NEGATIVE tests
  (deny + cross-tenant leakage). Use Testcontainers for DB-backed features.

END-OF-TURN PROTOCOL (critical for goal mode): finish EVERY turn with a status block:
  - what you changed this turn,
  - the result of `./mvnw verify` and (when relevant) `./mvnw verify -Dnative` with the pass/fail tail,
  - then exactly ONE final line:
      GOAL-COMPLETE: <phase> — JVM green, native green, all acceptance criteria met
    only when truly done, otherwise:
      GOAL-INCOMPLETE: remaining — <short list>
Never print GOAL-COMPLETE unless the build is actually green and every criterion is proven.

Acknowledge these rules. Do not start Phase 1 yet — I'll set it as a goal next.
```

---

## STEP 2 — Run the phases, one `/goal` each

### Phase 1 (paste this `/goal`)
```
/goal Implement Phase 1 of quarkus-cms per REPO/docs/HERMES_BRIEF.md Section 5 "Phase 1 — Type
Registry + Document Service + REST", working in REPO. Build-time discovery of @ContentType classes
into Panache entities + Flyway schema; DocumentService CRUD; dynamic REST adapter with
Strapi-compatible query params (filters, sort, pagination, fields, populate) returning the
{data,meta} envelope; collection + single types; relations (1-1, 1-n, n-n) and components +
dynamic zones; auto-generated OpenAPI. Add @QuarkusTest tests (Testcontainers Postgres) for every
endpoint, all five query features, and negative cases. Update PROGRESS.md, commit on branch
feat/phase-1-core. ACCEPTANCE (all must be TRUE with command output shown): (1) ./mvnw verify GREEN;
(2) ./mvnw verify -Dnative GREEN; (3) adding a @ContentType class yields working REST CRUD;
(4) all five query features have passing tests; (5) relations + components + dynamic zones persist
and populate in tests. Follow the END-OF-TURN PROTOCOL: end with GOAL-COMPLETE: Phase 1 ... only
when all five pass, else GOAL-INCOMPLETE: remaining — <list>. Do not bypass the DocumentService;
do not add schema.json as a model source.
```

### Phases 2–8 — reusable template
For each next phase, copy this and fill `<N>`, `<TITLE>`, the **criteria from the brief's Section 5**,
and a branch name:
```
/goal Implement Phase <N> of quarkus-cms per REPO/docs/HERMES_BRIEF.md Section 5 "Phase <N> — <TITLE>",
working in REPO. Build exactly what that phase specifies. Add @QuarkusTest tests for every new
endpoint/behavior including NEGATIVE/deny and (where relevant) CROSS-TENANT leakage cases. Update
PROGRESS.md and DECISIONS.md (new ADR if you deviate); commit on branch feat/phase-<N>.
ACCEPTANCE — copy the bullet criteria from that phase in the brief, AND always require:
./mvnw verify GREEN and ./mvnw verify -Dnative GREEN with output shown. Follow the END-OF-TURN
PROTOCOL: GOAL-COMPLETE: Phase <N> ... only when every criterion passes, else GOAL-INCOMPLETE:
remaining — <list>. Constraints unchanged: code-first modeling, all access via DocumentService,
RLS + tenant filters never bypassed, native verified this phase.
```

Phase titles to drop in: **2** Admin panel: Content-Type Builder (codegen) + Content Manager · **3**
Auth, RBAC, Row-Level Security & Multi-tenancy · **4** GraphQL API · **5** Media Library · **6**
Draft/Publish, History, i18n & Workflow engine · **7** Extensibility: lifecycle hooks, webhooks,
plugin SPI · **8** Hardening, docs, native & release.

### Tighten a running loop without resetting it — `/subgoal`
If mid-phase you want to add a criterion, append it (doesn't reset the loop; goal isn't `done`
until the original goal **and** every subgoal are met):
```
/subgoal Add a cross-tenant leakage test proving tenant A cannot read tenant B's media via the REST API
/subgoal Add a regression test for the row-policy bypass you just fixed
```
`/subgoal` (no args) lists them · `/subgoal remove <N>` · `/subgoal clear`.

---

## STEP 3 — Operate the loop

- **When you see `⏸ Goal paused — 40/40 turns used`:** the phase just needs more turns. Send
  `/goal resume` (resets the counter) and it keeps going. Big phases (1, 3, 6) may need a few resumes.
- **Phase-gated review (recommended first run):** when a phase hits `✓ Goal achieved`, review the
  diff and the negative tests yourself before pasting the next phase's `/goal`. Gate phases **3, 4,
  and 6** especially — that's where RLS and cross-tenant isolation live.
- **False `✓ Goal achieved` (judge wrong, work remains):** just send a normal message — e.g.
  "Native build is red — `<paste tail>`. Not done; fix and re-verify." — or re-issue a tighter
  `/goal`. The end-of-turn `GOAL-COMPLETE/INCOMPLETE` line makes this rare.
- **Check progress any time:** `/goal status`. Pause without losing state: `/goal pause`. Abandon:
  `/goal clear`. These are safe mid-run.
- **Optional parallelism:** for independent slices (e.g. the admin SPA vs. backend in Phase 2),
  Hermes can delegate to subagents / its Kanban board — but keep one `/goal` as the integrating
  objective so the judge still gates the phase as a whole.

---

## Quick copy-paste sequence

```
# 0. (config.yaml once)         -> goals.max_turns: 40 ; goal_judge -> a cheap fast model
# 1. normal message            -> STEP 0b  (unzip, read brief, confirm build)
# 2. normal message            -> STEP 1   (prime the rules + end-of-turn protocol)
# 3. /goal ...                 -> Phase 1  (STEP 2)
#    /goal resume              -> as needed when budget hits
# 4. review -> /goal ...       -> Phase 2  (template), then 3,4,5,6,7,8
```

That's the whole operating procedure: prime once, then one `/goal` per phase with the phase's
own acceptance criteria and the mandatory `GOAL-COMPLETE`/`GOAL-INCOMPLETE` end line that lets
Hermes' judge gate each phase correctly.
```
