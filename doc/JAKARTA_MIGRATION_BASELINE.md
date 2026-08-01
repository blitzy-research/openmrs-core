# Jakarta Migration Baseline and Deviation Register

This document is the **evidence artifact** for the completion-and-hardening pass that finished
openmrs-core's move off the `javax.*` / Spring-5 / Hibernate-5 generation onto the `jakarta.*`
namespace with a current Spring Framework, Hibernate ORM and Hibernate Validator stack on Java 21.
Its purpose is evidence and honesty, not persuasion: it records what was measured, what was changed,
what was deliberately **not** changed, and what could not be reproduced at all.

## Why This Document Exists

This is the only file created anywhere in the change. Three clauses put it here:

- **The Testing Requirements clause** — existing suites are the oracle; no assertion weakened; no
  test deleted; baseline captures shipped as comparison evidence; every adapted test-infrastructure
  change documented with its removed-API reason. Sections (a) and (f) discharge it.
- **Rule 6** — preserve observable outcomes where the new stack cannot reproduce baseline behaviour
  exactly, and document every gap. This document **is** that deviation register; section (d) carries it.
- **TR7 (structure freeze)** — no file is created, moved, renamed or deleted *except the single
  documentation artifact*. This file is that single exception, so no other file was created.

Every other file in the change was modified in place. Nothing was moved, renamed or deleted.

## Rules Provenance

`review_rules` returns **"No user rules provided."** It was called twice — the second time with an
explicit full line range — and returned that both times. **There is no separate on-disk user-rules
document for this project.**

The binding rule inventory is therefore the Agent Action Plan's own embedded **RULES** block:

- **Rules 1-6** — attributability; no transformation shims; no transitive javax smuggling; preserve
  the DAO structure; document pre-existing bugs rather than fixing them; preserve observable outcomes.
- **Transformation rules TR1-TR10** — including TR3 (single version control point), TR4 (exclusion
  over replacement), TR5 (coupled edits land together), TR7 (structure freeze) and TR10 (evidence
  before and after).
- **The boundary PRESERVE / EXCLUDE clauses** and **the Testing Requirements clause**.

The absence of a rules document was **not** treated as permission to lower the bar. Enterprise-standard
practice supplements the rules only where they are silent, and **no rule was invented**.

## Framing: What Was Actually Measured

### 1. The Demanded Baseline Is Unobtainable

The requirement asked for baseline captures taken against a **Hibernate-5 / Spring-5 / `javax.*`**
baseline. The base commit is already **past** that baseline — it sits at **Spring Framework 7.0.7**
and **Hibernate ORM 7.3.2.Final**, with zero `javax.*` namespace imports in source. A Hibernate-5
capture therefore cannot be taken from this tree at all.

The honest, verifiable substitute is a **base-commit capture plus a post-change re-capture**, which is
what this document ships. It is stated here explicitly rather than implying a Hibernate-5 comparison
that was never made. That substitution is itself recorded as a Rule 6 deviation in section (d).

### 2. The Pinned Versions Are a Floor, Not a Ceiling

The requirement pinned Spring 6.2.x, Hibernate ORM 6.6.x, Hibernate Validator 8.x, `jakarta.servlet`
6.x, `jakarta.validation` 3.x and `jakarta.persistence` 3.x. Those pins were read as a **minimum
floor that the dependency graph must meet or exceed**, never as a downgrade target. Downgrading was
forbidden on three independent grounds:

1. **The PRESERVE clause** — framework-migration work already merged at the base commit is fixed
   baseline: *build on it, do not restructure it*. A downgrade would delete merged work.
2. **The lock-step version chain** — Spring 7.0.7 requires Hibernate ORM 7.3.2.Final, which requires
   Hibernate Search 8.3.0.Final, which requires Lucene 10.4.0, alongside Infinispan 15.2.6.Final.
   None can be bumped, or dropped, in isolation.
3. **The floor is exceeded at the API-generation level**, not merely at the implementation level —
   see the comparison table in section (c).

### 3. Toolchain

Measured in the environment that produced every capture in this document:

| Component | Measured value | Source of the requirement |
|---|---|---|
| JDK | **JDK 21.0.11** (`build 21.0.11+10-1-25.10.2-Ubuntu`) | `maven.compiler.release` 21 in the root `pom.xml` |
| `JAVA_HOME` | `/usr/lib/jvm/java-21-openjdk-amd64` | the `.github/workflows` Java 21 + 25 matrix |
| Maven | **Maven 3.9.9** via the repository's own `./mvnw` wrapper | wrapper `distributionUrl` pins `apache-maven-3.9.9` |
| Enforcer floor | Maven 3.8.0 (`requireMavenVersion`) | root `pom.xml` |

Both values were re-measured with `java -version` and `./mvnw -version` rather than assumed, and both
match the versions recorded during planning. The compiler release level is fixed at **21** and was not
raised.

### 4. Which Commit Every Comparison Is Taken Against

**All before/after comparisons in this document are taken against
`3934d8086c684269e935562f25e806be91947115`** — the Agent Action Plan's base commit and the parent of the
first change in this work. Every `"$BASE"` in every command block in this document is that hash.

**Correction.** An earlier revision of this section said that
`2cbf9d7f8762451bdf253455c6e988eef732b843` — the baseline a review pass over this change nominated —
"does not resolve in this repository". **That statement was false and is withdrawn.** The object resolves,
and it is not an obscure one:

```text
  git cat-file -t 2cbf9d7f8762451bdf253455c6e988eef732b843   ->  commit
  git log -1 --format='%s %ci' 2cbf9d7f8
      Restore the frozen test corpus to the migration baseline   2026-08-01 03:48:52 +0000
  git rev-parse 9124e3dab^                                   ->  2cbf9d7f8762451bdf253455c6e988eef732b843
```

It is the **direct parent of `9124e3dab`**, the commit that created this document. That relationship is
stated without an ordinal or a total on purpose: an earlier revision called it "the seventh of the nine
commits on top of the base commit", and the total was already wrong when it was written and goes wrong again
with every commit added. `git rev-parse 9124e3dab^` is the form of the claim that cannot go stale, which is
why it is the form quoted above. No figure in this document was ever derived from a failed lookup; the
erroneous sentence was a wrong claim about Git history, not a corrupted measurement.

**Which baseline supports which measurement.** The two candidate baselines are not interchangeable, and the
difference between them is exactly one file:

| Baseline | What it is | What it supports |
|---|---|---|
| `3934d8086c684269e935562f25e806be91947115` | the Agent Action Plan's base commit ("chore: add TechDocs scaffold (#1)", 2026-05-15), parent of the first change in this work | **every "pre-change" column and every `"$BASE"` in every command block in this document**, including the section (e) `git archive` extraction of the pre-change changelog bytes |
| `2cbf9d7f8762451bdf253455c6e988eef732b843` | the tree with **all** code and configuration changes already applied and this evidence document not yet written | the review pass that nominated it; **no measurement in this document is taken against it** |

That second row carries a consequence worth stating, because it makes the choice of baseline immaterial for
every code claim. `git diff --name-status 3934d8086..2cbf9d7f8` reports **18** paths — every POM,
descriptor, Java and `NOTICE.md` edit in this work — so **all 18 code and configuration changes are already
present at `2cbf9d7f8`**. Comparing forward from there, `git diff --name-status 2cbf9d7f8` reports **four**:
this document, added; and the three Spring context descriptors whose leading indentation was subsequently
restored to its pre-`1f5303b89` form — one changed line each in `applicationContext-service.xml` and
`openmrs-servlet.xml`, seven in `webModuleApplicationContext.xml`, with every file byte-identical to its
`1f5303b89^` state. Those three edits are whitespace-only and cannot move a build, test, dependency-tree or
schema measurement, so a measurement taken at `2cbf9d7f8` still agrees with one taken here. Note the
working-tree form of that command, without `..HEAD`: it reports the same four paths whether or not the final
commit has yet landed, whereas the `..HEAD` form reported **one** path while those three restorations were
still uncommitted and would have quietly become wrong. A review comparing against `2cbf9d7f8` and this
document comparing against `3934d8086` therefore see the same code; only the "pre" side differs, and only
`3934d8086` gives a genuine pre-change "pre" side.

Where a count in the Agent Action Plan disagrees with a count measured at `3934d8086`, the measured value
is used and the discrepancy is stated at the point of use.

## Section (a): Test Baseline and Post-Change Re-Capture

The existing corpus is the oracle. Both captures below were produced with `./mvnw test -B` on the
toolchain above. The post-change column is the **measured** result of the run performed after every
edit in this change had landed.

| Module | Tests run (pre) | Failures | Errors | Skipped | Tests run (post) | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|---|---|
| `openmrs-api` | 4,929 | 0 | 0 | 45 | 4,929 | 0 | 0 | 45 |
| `openmrs-web` | 146 | 0 | 0 | 0 | 146 | 0 | 0 | 0 |
| `openmrs-liquibase` | 24 | 0 | 0 | 0 | 24 | 0 | 0 | 0 |
| `test-suite-module-api` | 6 | 0 | 0 | 0 | 6 | 0 | 0 | 0 |
| `test-suite-module-omod` | 1 | 0 | 0 | 0 | 1 | 0 | 0 | 0 |
| **TOTAL** | **5,106** | **0** | **0** | **45** | **5,106** | **0** | **0** | **45** |

The post-change run reproduced the baseline exactly: **5,106 run, 0 failures, 0 errors, 45 skipped**,
`BUILD SUCCESS`, with zero `[ERROR]` lines and zero `BUILD FAILURE` lines in the log. Per-module
attribution was taken by parsing each `Results:` block against the `Building <module>` line preceding
it, and the total was summed arithmetically rather than transcribed.

### 1. Build Measurements

Every figure below comes from **one identifiable capture**, described in full so it can be checked rather
than taken on trust. Earlier revisions of this section twice failed that standard — first reporting an
`install` of 1:21 and a `test` of 10:07 without saying which run produced them, then attributing a set of
figures to a commit whose tree had since stopped matching the one that ships. Both are superseded here by a
single set re-measured against the tree as it actually stands.

**The capture.** Commit `4192f776306ea2d5c8a362c93b6142faa1df3054` plus this change's own four modified
files in the working tree, which `git diff --name-only HEAD` enumerates in full: the three Spring context
descriptors whose leading indentation was restored
(`api/src/main/resources/applicationContext-service.xml`, `web/src/main/resources/openmrs-servlet.xml` and
`test-suite/module/omod/src/main/resources/webModuleApplicationContext.xml`) plus this document. Nothing
else differs — no other source, POM, resource or test file — so the captured tree **is** the tree that
ships, and the figures need no argument about whether later edits invalidated them.
Run on **2026-08-01** on **JDK 21.0.11** (`OpenJDK Runtime Environment build 21.0.11+10-1-25.10.2-Ubuntu`)
with **Apache Maven 3.9.9** via the repository's own `./mvnw`, against a **fully warmed** local repository,
with `CI` **unset** so Spotless runs in `apply` mode exactly as a developer's build does. Four commands,
run in this order, each captured to its own log:

| # | Command | Result | `Total time`, run 1 | `Total time`, run 2 | Exit |
|---|---|---|---|---|---|
| 1 | `./mvnw -B clean install -DskipTests` | `BUILD SUCCESS`, **13 of 13** reactor projects `SUCCESS` | **01:20 min** | **01:21 min** | **0** |
| 2 | `./mvnw -B test` | `BUILD SUCCESS`, **5,106 run / 0 failures / 0 errors / 45 skipped**, 13 of 13 `SUCCESS` | **10:42 min** | **10:47 min** | **0** |
| 3 | `./mvnw -B dependency:tree` | `BUILD SUCCESS`, **1,620 lines**, **zero** `javax.` occurrences of any kind | **2.474 s** | **2.389 s** | **0** |
| 4 | `./mvnw -B spotless:check -Dspotless.check.skip=false` | `BUILD SUCCESS`, **0 violations**, **1,273** `.java` files reported clean | **2.276 s** | **1.749 s** | **0** |

**Why two runs are published, and why a single wall clock would have been a figure you could not check.**
All four commands were executed twice, on the same tree and the same host: run 1 when this evidence was
first captured, run 2 as an independent re-execution of the whole A1-A10 gate block against the final
bytes. Every *outcome* column reproduced **exactly** — 13 of 13, 5,106 / 0 / 0 / 45, 1,620 tree lines,
zero `javax.`, zero violations, 1,273 files clean — while every *wall clock* moved slightly, by +1 s,
+5 s, −85 ms and −527 ms respectively. That is the honest shape of this measurement, and it carries a
practical warning: gate A8 **overwrites** `install.log` and `test.log`, so anyone who re-runs it will
find timings that differ from run 1 in the second decimal place and will *not* find run 1's exact
figures in the logs any more. Only the outcomes are asserted to be reproducible; the wall clocks are
published as a pair precisely so that no reader mistakes one uncontrolled sample for a benchmark, and
so that a re-run producing 01:21 rather than 01:20 is visibly expected rather than a contradiction.

Command 2 is the run behind the **post-change** columns of the per-module test table at the head of this
section, and its 5,106 total is a sum of the five `Results:` blocks rather than a transcription: 4,929 +
146 + 24 + 6 + 1, with the 45 skips all in `openmrs-api`. Its wall clock is dominated by two modules —
`openmrs-api` **08:49 min** and `openmrs-web` **01:23 min** — with the remaining eleven reactor projects
totalling **28.752 s** between them. Run 2 reproduced that distribution rather than the exact numbers:
`openmrs-api` **08:53 min**, `openmrs-web` **01:24 min**, the other eleven **29.251 s**. In both runs the
two heavy modules account for the same share of the total and the per-module test counts are identical.

> **On comparing wall clocks — this document does not.** The pre-change figures (`install` 2:16, `test`
> 10:17) were taken during planning on a different day, and the post-change figures above were taken on a
> warmed repository on a **shared** host. Neither run was a controlled benchmark: nothing pinned CPU
> contention, and the pre-change run had downloads the post-change run did not. So the earlier claim that
> the `install` phase is "measurably faster" is **withdrawn** — the numbers are not comparable at that
> resolution, and a wall-clock difference between two uncontrolled runs is not evidence of anything. What
> *is* claimed is the only thing the evidence supports: both phases completed, both exited **0**, and
> neither shows a change of *order* (minutes stayed minutes; nothing went from one minute to ten).
>
> **`[ERROR]` lines differ by phase, and the distinction matters.** Command 2 (`test`) contains **zero**
> `[ERROR]` lines and **zero** `BUILD FAILURE` lines. Command 1 (`install`) contains **309** `[ERROR]`
> lines and still exits 0 — every one of them is a **SpotBugs finding** (**284** `Medium`, **25** `High`),
> emitted because `install` reaches the `verify` phase where `spotbugs:check` is bound. They are
> non-failing by explicit configuration: the root `pom.xml` sets `<failOnError>false</failOnError>` on
> `spotbugs-maven-plugin`, carrying the standing comment
> *"TODO Set to true once existing findings are resolved"*. The count is **unchanged from the pre-existing
> baseline of 309** recorded on the pristine base commit during environment setup, which is the regression
> check that matters here: **this work introduced no new SpotBugs finding.** That is expected rather than
> lucky — every Java edit in this change is javadoc or comment text, which SpotBugs does not analyse.
>
> The per-module test totals were cross-checked two independent ways: by parsing each `Results:` block in
> command 2's log against the `Building <module>` line preceding it, and by aggregating the **335** Surefire
> XML reports that run wrote (`tests=5106 failures=0 errors=0 skipped=45`). Both agree exactly, and the
> `spotbugs:check` binding responsible for command 1's `[ERROR]` lines is at `phase=verify`, which command 2
> never reaches — which is why the two logs differ.

### 2. Which Maven Plugins Were Actually Exercised

An earlier revision of this document claimed that **all 23** managed Maven plugins "executed successfully
on JDK 21 across both a full `install` and a full `test` run". **That was false and is withdrawn.** The
inventory below was extracted from the four retained logs by matching Maven's goal-execution lines
(`[INFO] --- <prefix>:<version>:<goal> (<id>) @ <module> ---`) and counting invocations.

**Exercised: 14 of the 23.**

| Managed plugin | Version | Goals executed (invocations) |
|---|---|---|
| `spotless-maven-plugin` | 3.4.0 | `apply` (24), `check` (25) |
| `maven-enforcer-plugin` | 3.6.2 | `enforce` (24) |
| `buildnumber-maven-plugin` | 3.3.0 | `create` (24) |
| `build-helper-maven-plugin` | 3.6.1 | `parse-version` (24) |
| `maven-resources-plugin` | 3.5.0 | `resources` (16), `testResources` (16) |
| `maven-compiler-plugin` | 3.15.0 | `compile` (16), `testCompile` (16) |
| `maven-surefire-plugin` | 3.5.5 | `test` (16) |
| `maven-jar-plugin` | 3.5.0 | `jar` (7), `test-jar` (12) |
| `spotbugs-maven-plugin` | 4.9.8.3 | `spotbugs` (12), `check` (12) |
| `license-maven-plugin` | 3.0 | `check` (12) |
| `maven-dependency-plugin` | 3.10.0 | `tree` (12), `unpack-dependencies` (2) |
| `jacoco-maven-plugin` | 0.8.14 | `prepare-agent` (4), `report` (4) |
| `maven-war-plugin` | 3.5.1 | `war` (1) |
| `maven-assembly-plugin` | 3.8.0 | `single` (1) |

**Unvalidated: the other 9.** These are *not* claimed to have been validated on JDK 21 by this work. Each
reason was read out of the POMs, not assumed:

| Managed plugin | Version | Why it never ran |
|---|---|---|
| `maven-deploy-plugin` | 3.1.4 | the `deploy` phase was never invoked (the build stops at `install`), and the four `test-suite` POMs additionally set `<skip>true</skip>` |
| `maven-source-plugin` | 3.3.1 | bound only inside the `release` profile, in `api`, `web` and `webapp` |
| `maven-javadoc-plugin` | 3.12.0 | bound only inside the `release` profile, plus a `<reporting>` entry that runs only in the `site` lifecycle |
| `maven-checkstyle-plugin` | 3.6.0 | declared in `pluginManagement` with configuration but **no `<executions>` anywhere**, so it has no lifecycle binding at all |
| `maven-release-plugin` | 3.3.1 | `pluginManagement` only; release-time goals |
| `maven-eclipse-plugin` | 2.10 | `pluginManagement` only; an IDE-descriptor goal invoked by hand |
| `sonar-maven-plugin` | 5.6.0.6792 | reachable only through the `sonar` / `sonar-cloud` profiles, neither activated |
| `lifecycle-mapping` | 1.0.0 | m2e IDE metadata; **never executed by the Maven CLI by design** |
| `liquibase-maven-plugin` | 4.33.0 | declared in `liquibase/pom.xml` with **configuration only and no `<executions>`** — a manual snapshot-generation harness. Note that the executed section (e) comparison did **not** use it: it drove the `liquibase-core` **4.32.0** CLI off the `api` classpath, so the 4.33.0 plugin remains unexercised. This is register item 7 |

Three plugins were also exercised that the root `pluginManagement` does **not** pin, so their versions come
from Maven's own defaults or from a module: `maven-clean-plugin` 3.2.0 (`clean`, 13), `maven-install-plugin`
3.1.2 (`install`, 13) and `maven-antrun-plugin` 3.2.0 (`run`, 2), alongside the OpenMRS module packaging
plugin `openmrs:1.0.1` (`initialize-module`, `package-module`) in `openmrs-test-suite-module-omod`. One
detail worth recording because it looks like a version skew and is not: `dependency:tree` ran as **3.10.0**
in twelve modules and as Maven's default **3.7.0** in exactly one — `openmrs-bom`, which declares no
`<parent>` and therefore inherits no `pluginManagement`.

### 3. Invariants That Held

- Surefire runs with **`testFailureIgnore=false`**, so a 100% pass rate is a hard gate — any failure
  fails the build.
- Because the baseline has **zero failures, zero test exclusions are permissible — there is nothing
  to grandfather.** The requirement's allowance for "exclusions limited to failures present at
  baseline" therefore evaluates to an allowance of **nothing**, and none was taken.
- **No assertion was weakened, no test was deleted, and no new `@Disabled` was added.** The only path
  under any `src/test/` tree that this change touches is `webapp/src/test/resources/override-web.xml`,
  a servlet-container **resource** descriptor, not a test. **Zero Java test files were modified.**
- **The skip ceiling is 45, and it reconciles exactly — but not by "expansion".** These are two different
  measurements and this document keeps them apart, because an earlier revision said 41 source annotations
  "expand" to 45 skipped methods, which is not what happens:

  **Measurement 1 — the source `@Disabled` census.** Taken by scanning every tracked `.java` file and
  excluding imports, javadoc and commented-out lines. (Recorded because the planning figure, "42
  annotations across 22 `api` test classes", does not match the tree.)

  | Scope | `@Disabled` annotations | Files |
  |---|---|---|
  | under `api/` | **41** | **21** |
  | `test-suite/performance` (`StartupPerformanceIT`, an IT the default `test` phase never runs) | 2 | 1 |
  | **repository-wide** | **43** | **22** |

  **6** of the 41 are **class-level**: `SerializedObjectDAOTest`, `Log4JCompatibilityTest`,
  `ModuleTestSuite`, `CreateConceptDictionaryDataSet`, `CreateCoreUuids` and `CreateInitialDataSet`.

  **Measurement 2 — the Surefire-discovered skip count.** Parsed from the **335** retained
  `TEST-*.xml` reports: **45 skipped**, all of them in `openmrs-api`.

  **The reconciliation, exactly.** Only *four* terms contribute, and one of them is not an annotation at all:

  | Contribution | Skips | Why |
  |---|---|---|
  | `SerializedObjectDAOTest` — class-level `@Disabled` | **10** | Surefire discovers it (name matches the default `*Test` pattern), so **one** annotation is reported as one skip per test method |
  | `Log4JCompatibilityTest` — class-level `@Disabled` | **1** | discovered; the class holds a single test method |
  | Method-level `@Disabled`, across 14 classes | **32** | `PersonNameValidatorTest` 8, `AdministrationServiceTest` 4, `OrderServiceTest` 3, `ConceptReferenceTermValidatorTest` 3, `ConceptDAOTest` 2, `EncounterServiceTest` 2, `UserServiceTest` 2, `HL7ServiceTest` 2, `PatientDAOTest` 1, `ProviderEditorTest` 1, `OpenmrsServiceTest` 1, `ObsBehaviorTest` 1, `VisitValidatorTest` 1, `ConceptServiceTest` 1 |
  | `OpenmrsProfileExcludeFilterTest` — **not `@Disabled`** | **2** | JUnit **assumption aborts**: `shouldBeIgnoredIfOpenmrsVersionDoesNotMatch` calls `assumeOpenmrsPlatformVersion("1.6.*")` and `shouldBeIgnoredIfModuleDoesNotMatch` calls `assumeOpenmrsModules("metadatasharing:1.2")`. An aborted test is reported as skipped. The class carries **zero** `@Disabled` annotations |
  | **TOTAL** | **45** | 10 + 1 + 32 + 2 |

  **The four remaining class-level annotations contribute nothing**, because Surefire never discovers those
  classes: `ModuleTestSuite`, `CreateConceptDictionaryDataSet`, `CreateCoreUuids` and `CreateInitialDataSet`
  match none of the default `*Test` / `Test*` / `*Tests` / `*TestCase` include patterns. Of the 35
  method-level annotations, **3** are in `ModuleUtilIT` — an `*IT` class the default `test` phase never
  runs — leaving the 32 above. The arithmetic therefore closes as
  **6 class-level + 35 method-level = 41 under `api/`**, of which **34** sit in classes Surefire actually
  runs (2 class-level + 32 method-level) and those 34 produce **43** reported skips; the remaining **2**
  skips are assumption aborts, not annotations. **43 + 2 = 45.**
- The ceiling did not grow. The identical census was taken at the base commit and at the changed tree
  and both report **43 annotations in 22 files repository-wide, 41 in 21 files under `api/`**. Since the
  two are equal, no `@Disabled` was added.
- Surefire's global `argLine` is load-bearing on Java 21 and was not altered. It is *composed* rather
  than literal — the root `pom.xml` declares
  `-Duser.language=en -Duser.region=US -Xmx1g ${customArgLineForTesting} -Djava.locale.providers=COMPAT`
  and sets `customArgLineForTesting` to
  `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`, so the
  effective value carries both `--add-opens` flags and the `COMPAT` locale providers.

### 4. What This Capture Also Serves As

The 5,106-test run **is** the service-output capture for read, validation and privilege behaviour. It
executes against fixed DBUnit reference datasets, so no new harness was built — which is why **zero
test files appear in the change set**. Section (f) records that finding formally.

## Section (b): The javax Dependency-Tree Scan

This section is the Rule 3 evidence, and it discharges TR4's requirement to *document* the exclusion.

### 1. The Entire Problem Was One Artifact With Six Carriers

Before the change the resolved graph contained exactly **one** artifact from the javax generation:
**`javax.xml.bind:jaxb-api:jar:2.3.1`**. It appeared six times, and every occurrence was a direct
child of `org.liquibase:liquibase-core:4.32.0`:

| Carrier module | Scope |
|---|---|
| `openmrs-api` | compile |
| `openmrs-web` | compile |
| `openmrs-webapp` | compile |
| `test-suite-module-api` | provided |
| `test-suite-module-omod` | provided |
| `test-suite-performance` | test |

### 2. A Version Bump Provably Cannot Remove the Leak

`liquibase-core` declares `javax.xml.bind:jaxb-api:2.3.1` **at compile scope directly in its own POM**,
excluding only `javax.activation:javax.activation-api`. Resolving the newer lines shows that **4.33.0
and 5.0.3 both still declare it**. Upgrading therefore cannot remove the leak.

This is why the requirement's clause *"Liquibase bumped only as far as Java 21 requires"* resolves to
**do not bump at all**: Java 21 requires nothing of it — 4.32.0 runs clean on JDK 21 through a full
green build — and the javax leak is version-independent. Moving it would risk changelog behaviour for
zero benefit.

### 3. The Remedy Was One `<exclusion>`

A single `<exclusion>` for `javax.xml.bind:jaxb-api` was appended to the **existing** managed
`liquibase-core` `<exclusions>` block in `bom/pom.xml`, which already held exactly one exclusion,
`ch.qos.logback:logback-classic`. That one edit clears all six carriers, because **every consumer
declares `liquibase-core` version-less and exclusion-less** — verified in both `api/pom.xml` and
`web/pom.xml`. Keeping those consumer declarations bare is therefore **load-bearing, not cosmetic**:
it is TR3, the single version control point, doing the work.

**Proof of mechanism rather than assumption:** `ch.qos.logback:logback-classic`, excluded by exactly
this mechanism in the same block, appears **zero** times in the resolved tree, while `liquibase-core`
itself appears **six** times. The mechanism was demonstrated in-repo before it was relied upon.

### 4. Post-Change Scan: Zero javax Nodes

```bash
  ./mvnw dependency:tree -B > deptree.txt

  # the requirement's own stated check - 0 matches BEFORE and AFTER (it already passed at baseline)
  grep -E "javax\.(servlet|persistence)" deptree.txt

  # stronger: ANY javax dependency-tree node - 6 matches BEFORE, 0 AFTER
  grep -E "^\[INFO\][^:]*[+\\|-]- javax\." deptree.txt

  # broadest sweep actually run here: the bare string, anywhere in the output - 0 AFTER
  grep -n "javax\." deptree.txt

  # gate A1 - the three removed coordinates must be gone
  grep -E "commons-fileupload|groovy-all|commons-fileupload2" deptree.txt

  # liquibase-core itself must SURVIVE (only its jaxb-api child is excluded)
  grep -c "liquibase-core" deptree.txt   # expect 6
```

Measured against the 1,620-line post-change capture: the requirement's grep returns **0**, the stronger
node grep returns **0**, and the broadest sweep finds **the string `javax.` nowhere in the entire
output**. The removed-coordinate grep returns **0**. `liquibase-core` survives with exactly **6**
occurrences, all at `4.32.0`, at the same compile/compile/compile/provided/provided/test scopes listed
above — confirming the exclusion removed only the `jaxb-api` child and nothing else.

Why the stronger check matters: the requirement's pattern covers only `servlet` and `persistence`, so
the one javax artifact actually present, `javax.xml.bind:jaxb-api`, was **invisible to it** and that
check passed at the base commit while the leak was still there. Rule 3 speaks to the old generation
**as a whole**, not to two of its packages.

**Why the exclusion is safe — bytecode, not substitution.** It is tempting to justify this by saying
`api/pom.xml` already declares `jakarta.xml.bind:jakarta.xml.bind-api` and `org.glassfish.jaxb:jaxb-runtime`,
so the Jakarta artifacts "replace" the removed one. **That reasoning is invalid** and is not relied on here:
`jakarta.xml.bind.*` and `javax.xml.bind.*` are different package names, so a Jakarta jar is not a binary
substitute for `javax.xml.bind` — any class still linking against the old package would fail with
`NoClassDefFoundError`, not silently resolve.

The real justification is that **nothing on the classpath links against `javax.xml.bind`**. Every `.class`
entry in every jar of the `api` module's runtime classpath was decompressed and searched for the constant
`javax/xml/bind`:

| Scan | Result |
|---|---|
| `liquibase-core-4.32.0.jar` — the artifact that declared the dependency | **0 of its 1,344 classes** reference `javax/xml/bind` |
| All **229** jars on the `api` runtime classpath | exactly **one** class in the entire graph references it |
| That one class | `com.thoughtworks.xstream.core.util.Base64JAXBCodec`, referencing only `javax.xml.bind.DatatypeConverter` |

And that single reference is unreachable on this runtime. Its only referrer is
`com.thoughtworks.xstream.core.JVM`, whose static initialiser picks a Base64 codec by **reflection**: it
calls `loadClassForName("…Base64JavaUtilCodec")` first and only falls back to `…Base64JAXBCodec` if that
returns `null`. `Base64JavaUtilCodec` is present in the same jar and links against `java.util.Base64`, which
has existed since Java 8 — so on Java 21 the JAXB codec is **never selected**, and the fallback probe is a
`loadClassForName` that returns `null` rather than throwing when the class is absent. Two independent
empirical confirmations: the **335**-report suite is green including `SimpleXStreamSerializerTest`
(9 tests, the only in-repo XStream consumer), and the section (e) Liquibase run applied **1,028 changesets**
with `jaxb-api` excluded from the classpath.

The declared Jakarta artifacts are still necessary — just for a different reason than "replacement". The
same bytecode scan shows the real consumer of the Jakarta API is **`hibernate-core-7.3.2.Final`, in 284
classes**; OpenMRS's own source imports `jakarta.xml.bind` **zero** times, exactly as it imported
`javax.xml.bind` zero times. So no Java file lost a capability, and none gained one.

### 5. A Documented False Positive

`javax.inject:javax.inject:1` can appear in **raw build logs**, in Maven's plugin-resolution
"present in the local repository" lines. It is **never a dependency-tree node** — the reactor resolves
`jakarta.inject:jakarta.inject-api:2.0.1`, which was confirmed present at provided, runtime and test
scopes. Readers must scan `dependency:tree` output, not raw build logs, and must not act on it.

> **Note:** this false positive did **not** reproduce in the capture taken here — `javax.inject` occurs
> zero times in the post-change `install` log, because the local repository was already warm and Maven
> emitted no resolution chatter at all. The caution is recorded for readers whose logs *do* contain
> those lines; it is not being reported as an observation made here.

### 6. The Three Removed Coordinates

| Coordinate | Graph footprint | Why removable |
|---|---|---|
| `commons-fileupload:commons-fileupload:1.6.0` | leaf, no children; `openmrs-web` (compile), `openmrs-webapp` (compile), `test-suite-module-omod` (provided) | a javax.servlet-generation API surface with **zero source imports**; multipart handling runs through Spring's `StandardServletMultipartResolver` in `web/src/main/java/org/openmrs/web/WebConfig.java` |
| `org.apache.commons:commons-fileupload2-jakarta-servlet6:2.0.0-M5` | drags `commons-fileupload2-core:2.0.0-M5`; same three modules | **zero source references**; a **milestone (non-GA)** artifact — no GA has ever been published |
| `org.codehaus.groovy:groovy-all:2.4.21` | leaf, no children; `openmrs-api` (compile), `openmrs-web`, `openmrs-webapp`, `test-suite-module-api` (provided), `test-suite-performance` (test) | **zero** `import groovy.` and **zero** `import org.codehaus.groovy.` repository-wide; Groovy 2.4 **predates Java 9** |

Each of those three is a **four-coupled-edit removal (TR5)**: the module POM declaration, the
`bom/pom.xml` managed entry, the `bom/pom.xml` version property and the `NOTICE.md` attribution line,
all landing in the same change. The mechanical consequence of omitting any one is immediate:

- omit the module declaration and the build fails on an unmanaged version;
- omit the BOM property and the build fails on an unresolvable `${...}` placeholder;
- omit the `NOTICE.md` line and the project attributes a library it no longer ships.

All four edits landed for all three coordinates. Post-change verification: the coordinates appear in
**no** POM in the reactor, in **no** dependency-tree node, and in **no** `NOTICE.md` line, while the
attributions that must survive — `commons-collections`, `liquibase-core`, the Infinispan entries,
`jakarta.xml.bind-api`, `jaxb-runtime` and `type-converter` — are all still present.

### 7. The False Positive Cleared Before Scheduling Those Removals

A textual scan for `fileupload|groovy` across `api/src`, `web/src` and `webapp/src` returns **36 hits**,
which initially looks like live usage. **All 36 are internationalization message keys** — two keys
replicated across **18** locale files, for example in `api/src/main/resources/messages.properties` —
naming `org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler`. **That class has zero `.java`
files anywhere in the repository.** Re-measured here: 36 hits across 18 files, **100% of them
`.properties` files**, with zero hits in any source file.

The three removals are therefore **source-neutral**, and the orphaned keys are logged as item 6 of the
pre-existing defect register rather than deleted, because deleting message keys carries no target-stack
attribution.

### 8. Correction: `com.sun:tools` Is Not a Four-Coupled-Edit Removal

`com.sun:tools:1.4.2` was verified to have **no `bom/pom.xml` managed entry, no `bom/pom.xml` version
property and no `NOTICE.md` attribution line** — the version was inlined directly in the profiles that
declared it. TR5 therefore does not apply to it.

Its retirement is a **single-file deletion of two permanently dead Maven profiles** in `tools/pom.xml`,
identified by their `<id>` values:

- **`default-tools.jar`** — activated by a `<file><exists>` check for `${java.home}/../lib/tools.jar`
- **`mac-tools.jar`** — activated by a `<file><exists>` check for `${java.home}/../Classes/classes.jar`

Both `tools.jar` and `classes.jar` were removed in **JDK 9**, so on Java 21 neither activation can ever
fire. Each profile declared the same `com.sun:tools:1.4.2` dependency at `system` scope. The profiles
are cited here by `<id>` deliberately: the line spans recorded during planning were checked against the
file and one was wrong — `default-tools.jar` does span L28-L44, but `mac-tools.jar` spans **L45-L61**,
not L45-L53. Identifiers are stable where line numbers are not.

## Section (c): Resolved Jakarta Artifact Set

### 1. The Twelve Jakarta API Artifacts

Enumerated from the post-change `dependency:tree` capture. Exactly **twelve** distinct `jakarta.*` API
artifacts resolve, and **zero** `javax.*` artifacts:

| Artifact | Version |
|---|---|
| `jakarta.persistence:jakarta.persistence-api` | 3.2.0 |
| `jakarta.servlet:jakarta.servlet-api` | 6.1.0 |
| `jakarta.validation:jakarta.validation-api` | 3.1.1 |
| `jakarta.annotation:jakarta.annotation-api` | 3.0.0 |
| `jakarta.transaction:jakarta.transaction-api` | 2.0.1 |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 4.0.5 |
| `jakarta.inject:jakarta.inject-api` | 2.0.1 |
| `jakarta.el:jakarta.el-api` | 5.0.0 |
| `jakarta.activation:jakarta.activation-api` | 2.1.4 |
| `jakarta.mail:jakarta.mail-api` | 2.1.5 |
| `jakarta.servlet.jsp:jakarta.servlet.jsp-api` | 4.0.0 |
| `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api` | 3.0.2 |

### 2. The Floor Is Exceeded at the API-Generation Level

The pins are not merely met by newer implementations; the **API generation itself** is newer than the
pinned line would have delivered:

| Framework artifact | Transitively supplies |
|---|---|
| `hibernate-core:6.6.54.Final` (the pinned line) | `jakarta.persistence-api` 3.1.0 |
| `hibernate-core:7.3.2.Final` (as found) | `jakarta.persistence-api` **3.2.0** |
| `hibernate-validator:8.0.5.Final` (the pinned line) | `jakarta.validation-api` 3.0.2 |
| `hibernate-validator:9.1.0.Final` (as found) | `jakarta.validation-api` **3.1.1** |

The requirement asked for `jakarta.persistence` 3.x and `jakarta.validation` 3.x; both are satisfied on
*higher* minors than the floor. `jakarta.servlet-api` **6.1.0** is the newest 6.x GA release in
existence — 6.2.0-M1 and 6.2.0-M2 are milestones, not GA.

### 3. Source-Side Census

- **Zero** matches for `^import javax\.(servlet|persistence|validation|annotation|transaction)` across
  the **1,273** tracked `.java` files (846 main, 427 test). **The requirement's validation item 5
  already passed at the base commit**, and it still passes.
- Jakarta imports: `jakarta.persistence` **963**, `jakarta.servlet` **138**, `jakarta.mail` **14**,
  `jakarta.annotation` **3**, `jakarta.activation` **3**, `jakarta.validation` **1**.

The remaining `javax.*` imports are **JDK-shipped and out of scope by definition**:

| Module | `javax.*` imports | Measured breakdown |
|---|---|---|
| `api` | **82** | `javax.xml` **61** (`javax.xml.transform` 33 + `javax.xml.parsers` 28), `javax.swing` **8**, `javax.imageio` **6**, `javax.crypto` **5**, `javax.sql` **2** |
| `web` | **12** | `javax.xml.parsers` **7**, `javax.xml.transform` **5** |
| `webapp`, `test`, `test-suite`, `tools`, `liquibase` | **0** each | — |

> **Note:** the sub-breakdown above is a **correction**. The figures carried in planning listed items
> summing to 75 against a stated `api` total of 82 — an internal inconsistency. Re-measuring by
> collapsing each import to its second-level package gives 61 + 8 + 6 + 5 + 2 = **82**, which reconciles.
> The correction is stated openly because an evidence document that quietly repeats an arithmetic error
> is worse than one that shows its working.

A second measurement note, for anyone who re-runs the census: counting with `find` instead of
`git ls-files` yields 1,288 rather than 1,273, because 15 untracked, git-ignored scratch files sit
outside `target/`. **1,273 is the tracked-file count**, it is identical at the base commit and at the
changed tree, and restricting the census to tracked files leaves `api` at 82, `web` at 12 and the
forbidden-import count at 0 unchanged.

### 4. The Obligation Inverted Into a Standing Guard

Because the source migration was already complete, the import-transformation requirement became a
**guard** rather than a task:

```text
  GUARD (applies to every file in every module):
    No file may introduce or reintroduce an import matching
      javax.(servlet | persistence | validation | annotation | transaction)
    Permitted javax packages are JDK-shipped only:
      javax.xml.*, javax.swing.*, javax.imageio.*, javax.sql.*, javax.crypto.*, javax.naming.*

  MECHANICAL ENFORCEMENT:
    spotless-maven-plugin 3.4.0, importOrder = java|javax,jakarta,org,com,,\#
    applied at process-sources on every build; the ci-checks profile flips it to
    check-only when env.CI=true.

  VERIFICATION:
    grep -rE "import javax\.(servlet|persistence|validation|annotation|transaction)" \
      --include=*.java .   ->  must return zero hits
```

The import order is cited here by its **value**, not by a line number: the reference recorded during
planning pointed at the `spotless-maven-plugin` element rather than at the `<importOrder>` element that
actually carries the value. Because this change's Java edits are javadoc-and-comment-only, no import
statement moved in any file — but the guard is stated so that it is not lost.

## Section (d): Documented Deviations and Declined Changes

Rule 6 requires that outcome-equivalence take precedence over mechanism-equivalence and that **every
gap be written down**. Three deviations are recorded, plus the Liquibase hold.

### 1. Deviation: The Infinispan Coordinate Lock

The second-level cache and the Spring cache manager stay on **`org.infinispan:infinispan-spring6-embedded`**
and **`org.infinispan:infinispan-hibernate-cache-v62`** at **15.2.6.Final**, *even under Spring 7 and
Hibernate 7*, because the newer-looking coordinates **do not exist**:

- `infinispan-spring7-embedded` does not exist below **16.1.3**.
- No `-hibernate-cache-v70` or `-v73` module has ever been published; that line tops out at `-v66`,
  which is 16.x only.

Renaming either artifact would produce a coordinate that cannot be resolved. The same artifact IDs are
mirrored verbatim in `NOTICE.md`, so a rename would also desynchronize the legal attribution.

**The observable outcome is preserved intact:** a working second-level cache via
`hibernate.cache.region.factory_class=infinispan` in `api/src/main/resources/hibernate.default.properties`;
configurations remaining on schema `urn:infinispan:config:15.2` in both `infinispan-api.xml` and
`infinispan-api-local.xml`; and exactly **seven** Infinispan imports, all confined to the single file
`api/src/main/java/org/openmrs/api/cache/CacheConfig.java`. The full build and the 5,106-test suite
pass, so the mismatch between the artifact name and the framework generation is **recorded, not changed**.

### 2. Deviation: The `uniqueResult` Exception Type

`JpaUtils.getSingleResultOrNull(Query)` in `api/src/main/java/org/openmrs/api/db/hibernate/JpaUtils.java`
catches **`jakarta.persistence.NoResultException`** and returns `null`, reproducing Hibernate
`Criteria.uniqueResult()`'s null-on-empty contract. A non-unique result, however, now surfaces
**`jakarta.persistence.NonUniqueResultException`** where Hibernate 5 raised
`org.hibernate.NonUniqueResultException`. The javadoc documents that propagation deliberately, with
`@throws NonUniqueResultException if more than one result is found`.

**Correction — the propagation is direct, and undecorated.** An earlier revision of this document claimed
both exception types "funnel through the OpenMRS-owned hierarchy — `DAOException extends APIException`".
That is **wrong, and it is withdrawn.** Re-measured, the actual mechanism is:

- `getSingleResultOrNull` has exactly one `catch`, and it catches **only** `NoResultException`.
  `NonUniqueResultException` is *imported solely so the `@throws` javadoc can name it* — it is never
  caught and never wrapped.
- The helper is called from **12 sites** in three DAOs — `HibernateHL7DAO` ×2, `HibernateConceptDAO` ×6,
  `HibernateUserDAO` ×4 — and **not one of them** sits inside a `catch` of `PersistenceException`,
  `HibernateException`, `RuntimeException` or `Exception`. (The single bare `catch (Exception e) {}` in
  `HibernateUserDAO` guards a `LuhnIdentifierValidator` call at line 223, not the query at line 234.)
- **No exception-translation infrastructure is registered anywhere.** A repository-wide
  `git grep PersistenceExceptionTranslationPostProcessor` returns **zero** hits, so although **22** DAO
  classes under `api/src/main/java/org/openmrs/api/db` carry `@Repository` — which would make them
  *eligible* for translation — no translating proxy is ever created for them, and no
  `DataAccessException` is ever produced. There is no Spring Boot on the classpath to auto-register one.
- The two are **distinct classes**, so a `catch` that names one does not match the other. Measured with
  `javap` on the artifacts actually on this classpath:
  `jakarta.persistence.NonUniqueResultException extends jakarta.persistence.PersistenceException extends
  java.lang.RuntimeException` (from `jakarta.persistence-api-3.2.0.jar`), and
  `org.hibernate.NonUniqueResultException extends org.hibernate.HibernateException` — which in
  `hibernate-core-7.3.2.Final.jar` itself `extends jakarta.persistence.PersistenceException`. So on the
  **current** classpath the two share the `PersistenceException` supertype; only the leaf class differs.
  Whether the Hibernate-5-era `org.hibernate.HibernateException` shared that supertype is **not asserted
  here** — no Hibernate 5 artifact exists in this environment to measure, and the claim is not needed:
  the leaf class differs either way, which is the whole of the deviation.

So what actually reaches a service caller on a non-unique result is the **provider's own
`jakarta.persistence.NonUniqueResultException`** — not a `DAOException`, not an `APIException`.

**What is therefore claimed, and what is not.** The narrower claims below are what the evidence supports;
the blanket "invisible at the service boundary" is not among them.

| Claim | Status | Evidence |
|---|---|---|
| No `org.openmrs.api.*` **signature** changes | **Proven** | both the baseline and the current exception are **unchecked**, so neither needs declaring; the **590** `throws DAOException` declarations across 38 files in `api/src/main/java/org/openmrs/api/db` are untouched, and `DAOException extends APIException extends RuntimeException` |
| The **null-on-empty** contract of `uniqueResult()` is reproduced exactly | **Proven** | the `NoResultException` → `null` catch, unchanged in this work |
| The **outcome class** is the same — a non-unique result yields no value and raises an unchecked failure rather than silently returning one row | **Proven** | there is no code path that swallows it |
| The existing corpus cannot observe the difference | **Proven, and narrow** | `git grep NonUniqueResultException` across `api/src/test`, `web/src/test` and `test-suite` returns **zero** hits, so no assertion in the 5,106-test corpus keys on the concrete type; this establishes only that the corpus is *silent* on it, not that the types are interchangeable |
| The **concrete exception type** a caller can `catch` by name is identical to the Hibernate-5 baseline | **NOT claimed — this is the deviation** | the leaf class differs, as measured above; a downstream caller that catches `org.hibernate.NonUniqueResultException` specifically would no longer match, because what is raised is `jakarta.persistence.NonUniqueResultException` |

That last row **is** the Rule 6 deviation: the *outcome* is preserved, the *mechanism and concrete type*
are not, and the difference is recorded here rather than papered over. It is not a defect to repair —
introducing a translation layer to restore the old type would be an unattributable architectural addition
that Rule 1 forbids, and it would change 12 call paths in the frozen DAO layer that Rule 4 protects.

**On adding a service-boundary test for this:** the review that produced this correction also suggested
adding one before asserting observable equivalence. The equivalence claim has instead been **narrowed to
what is already proven**, which removes the need for new evidence. Adding the test is separately
**declined on AAP grounds, with citation**: the test corpus is frozen at exactly **5,106 run / 0 / 0 / 45**
by AAP §0.2.2.2 ("no assertion weakened, no test deleted, no new `@Disabled`"), §0.8.9 and §0.10.3.2, and
this document's own change inventory records **zero test files modified**. Expanding the corpus is
precisely the deviation an earlier review flagged as critical. The deviation stands recorded, with its
exact limits stated above.

### 3. Deviation: Baseline-Capture Provenance

A true Hibernate-5 capture is unobtainable, for the reason given in the framing section. Base-commit and
post-change captures ship instead, and that substitution is stated explicitly rather than implied.

### 4. The Liquibase Hold

`org.liquibase:liquibase-core` is held at **4.32.0**. The justification is that a bump serves neither
goal: it does not serve Java 21, because 4.32.0 already runs clean on JDK 21 through a full green build;
and it does not remove the `jaxb-api` leak, because 4.33.0 and 5.0.3 both still declare it. Moving it
would risk changelog behaviour for zero benefit.

One accuracy note that matters for anyone reasoning about Liquibase here: the `liquibase-maven-plugin` is
pinned at **4.33.0** while the `liquibase-core` library is **4.32.0**. That skew is real, pre-existing,
and harmless — the plugin runs only in the `openmrs-liquibase` module's manual snapshot-generation
configuration, never in the default lifecycle. It is item 7 of the defect register.

### 5. Changes Deliberately Declined

Recorded so that Rule 1 is visible operating as a brake rather than as an excuse:

| Candidate | Decision | Reason |
|---|---|---|
| `commons-collections:commons-collections` 3.2.2 | **kept** | **live, not dead** — 30 source imports (`CollectionUtils`, `ListUtils`, `MapUtils`, `Predicate`, `comparators.ComparatorChain`, `comparators.NullComparator`, `set.ListOrderedSet`), including the raw `org.apache.commons.collections.Predicate` in `AttributeMatcherPredicate`. Migrating to `commons-collections4` has no Spring/Hibernate/Jakarta/Java-21 attribution, so Rule 1 excludes it |
| `org.slf4j:slf4j-api` / `jcl-over-slf4j` 1.7.36 | **kept** | SLF4J is not a namespace concern, so there is no attribution |
| Spring 7.0.8 | **declined** | exists, but no validation item requires it and it would force re-validation of the whole lock-step chain |
| Hibernate ORM 7.3.12.Final | **declined** | same reason |
| Hibernate Validator 9.1.3.Final | **declined** | same reason |
| `com.fasterxml.jackson.core:jackson-annotations` `2.21` | **kept as pinned** | the missing patch component is **deliberate and correct, not a skew**. The registry check is *this document's* evidence, not the BOM's — re-verified directly against Maven Central: `.../jackson-annotations/2.21/jackson-annotations-2.21.pom` returns **HTTP 200** and `.../2.21.2/jackson-annotations-2.21.2.pom` returns **HTTP 404**, and the local repository correspondingly holds only `2.21`. (The `2.21.2` pinned by `jacksonVersion` at `bom/pom.xml:59` applies to `jackson-core`, `jackson-databind`, `jackson-datatype-jsr310` and `jackson-datatype-hibernate7`, all of which do publish that patch — `jackson-annotations` is the single artifact that opts out, with its own literal version.) The BOM records only that the omission is intentional — its inline comment at `bom/pom.xml:333` reads, in full, `<!-- No patch version -->`; it does **not** carry the 404/200 rationale, and it was not edited to add it, because a comment expanding on registry availability has no Spring/Hibernate/Jakarta/Java-21 attribution and Rule 1 excludes it |
| All 23 managed Maven plugins | **unchanged** | no plugin bump is attributable to the target stack. **14 of the 23 were exercised on JDK 21** and passed; the other **9 never run in the executed lifecycles** and are therefore recorded as *unvalidated*, not as validated. The per-plugin inventory is in section (a).2 — an earlier revision of this document claimed all 23 executed, which was false |

### 6. Research Method

`web_search` returned no usable results for the intended version queries, so no external version claim
in this change rests on it. All external version facts were obtained from **primary registry endpoints**
— Maven Central's `maven-metadata.xml` and `.pom` endpoints under `https://repo1.maven.org/maven2/`,
plus the OpenMRS Nexus repository declared in the reactor, which is where the BOM-only private
coordinate `org.openmrs.liquibase.ext:type-converter:1.0.1` resolves from. **No version claim is
asserted from memory.**

## Section (e): Clean-Database Liquibase Run and Schema Diff

### 1. EXECUTED — Measured Result, Not a Procedure

**This comparison was executed, and the diff is empty.** The environmental note below explains why the
planning phase expected to be unable to run it, and why that limitation no longer applies.

- In the **planning environment** no `mysql`, `mysqld` or `mysqldump` binary was present at all, and
  neither were `mariadb` or `mariadb-dump`. That is why the item was originally specified as a procedure,
  and that historical statement is **phase-labelled here rather than repeated in the present tense**.
- In the **execution environment** the client tooling *is* present — `mysql`, `mysqldump`, `mariadb` and
  `mariadb-dump` all resolve, from a **MariaDB 11.8.3** client package — and a **`mariadb:10.11.7`**
  server (`10.11.7-MariaDB-1:10.11.7+maria~ubu2204`) is reachable on `127.0.0.1:3306`. Only the server
  binaries `mysqld` and `mariadbd` are absent, because the server runs in a container.

The "pre-change" side is genuine without touching the working tree: the base commit's changelog bytes were
materialised with `git archive 3934d8086`, and the two trees were then compared in two stages, because
byte equality alone is not sufficient evidence. First the **sorted file sets** were compared — `diff -u`
of the base and current `find`-and-`sort` listings produced **zero lines**, so no changelog was added,
removed or renamed on either side — and only then was each of the **38** files compared with `cmp`:
**38 compared, 0 differing**. The two-stage order matters, and the earlier single-stage form is
**withdrawn**: a loop driven by the base listing cannot, even in principle, notice a changelog that exists
only in the current tree.

The two sides are also **symmetric in what they expose to the engine**, which an earlier revision was not.
That revision handed the current side the whole live resource root, `api/src/main/resources` — which
carries far more than changelogs, including `META-INF/services` registrations for
`liquibase.logging.LogService`, `liquibase.change.Change`, `liquibase.sqlgenerator.SqlGenerator` and
`liquibase.datatype.LiquibaseDataType` whose implementing classes live in the `api` module's own output
and are therefore **absent** from a dependency-only classpath. The measurable consequence was visible in
the retained evidence: that side raised `java.util.ServiceConfigurationError: liquibase.logging.LogService:
Provider liquibase.ext.logging.slf4j.Slf4JLogService not found`, fell back to `java.util.logging`, and
produced a **1,304-line** log against the other side's **1,169** — a difference in log plumbing, not in
DDL, but a difference in something other than the changelog bytes under comparison, which is exactly what
this comparison must not have. The current side is now materialised into a root holding the changelog
closure and nothing else (`liquibase-*.xml` plus `org/openmrs/liquibase/**`, copied from the working tree
and re-verified file-by-file with `cmp` — **38 copied, 0 differing**), so the only difference between the
two roots is the bytes being compared. The payoff is a much stronger assertion, which the script now makes
and which passed: **with timestamps stripped, the two engine logs are identical** — `diff` produced
**0 lines** over **1,169 lines each** — so the same 1,028 changesets ran in the same order and emitted the
same 116 warnings on both sides, and neither log contains a `ServiceConfigurationError`.

Both sides were then installed into
their own **disposable, per-run** database by the **same** Liquibase engine — `liquibase-core`
**4.32.0** (`version 4.32.0 #8159 built at 2025-05-19`), resolved from the `api` module's own classpath so
that engine parity between the two sides is guaranteed by construction — driving
`liquibase-schema-only.xml` with the platform's own bookkeeping table names. Those two names, the target
URL and the credentials are all **properties inside the mode-600 defaults file** that
`liquibase.integration.commandline.Main` reads via `--defaultsFile`, not command-line options:
`databaseChangelogTableName=liquibasechangelog` and
`databaseChangelogLockTableName=liquibasechangeloglock`, matching
`DatabaseUpdater.setDatabaseChangeLogTableName`. The complete Liquibase command line carries only
`--defaultsFile=<path>` and the `update` verb. Each database was then dumped with
`mysqldump --no-data --skip-comments --skip-dump-date`.

The measurements below are the ones this run produced — run identifier **`eefb32d9fa1d`**, with every raw
artifact retained by the run itself under `./v3-evidence-eefb32d9fa1d/`. Every figure is reproducible with
the script in (e).4, which is the exact script that produced them.

| Measurement | Before side (base-commit changelog bytes) | After side (current changelog bytes) |
|---|---|---|
| Changesets executed (`UPDATE SUMMARY` in the engine log) | **1,028** run, 0 previously run, 0 filtered out | **1,028** run, 0 previously run, 0 filtered out |
| `Running Changeset:` lines in the engine log | **1,028** | **1,028** |
| Rows in `liquibasechangelog` | **1,028** | **1,028** |
| Base tables | **119** | **119** |
| Columns | **1,510** | **1,510** |
| Distinct indexes | **697** | **697** |
| Foreign keys | **446** | **446** |
| `CREATE TABLE` statements in the dump | **119** | **119** |
| Dump size | **3,389 lines / 175,156 bytes** | **3,389 lines / 175,156 bytes** |
| MD5 of the dump | `659f521033a44a4b95e57fe0bbe105a9` | `659f521033a44a4b95e57fe0bbe105a9` |

**`diff` exited 0 with zero differing lines**, and `cmp` reports the two dumps **byte-identical**.

Each figure above was read back out of a named file rather than transcribed from a terminal, so none of them
rests on recollection. Every value in the table above and the one below was verified against these files
twice: once when the run finished, and again during the final validation sweep. The directory has since been
removed, exactly as the guidance in section (e).3 directs — it is an untracked 15 MB byproduct that must not
enter a commit, so it is deliberately **not** present in a fresh clone, and the file names below are a
description of what a re-run produces rather than a path you can list today. Re-running the script in
(e).4 recreates all of it under a new run identifier. The recorded run produced, under
`./v3-evidence-eefb32d9fa1d/`:

| Artifact | Contents as produced by the recorded run |
|---|---|
| `changelogs-base.txt`, `changelogs-current.txt` | the two sorted changelog listings, 38 paths each |
| `changelogs-fileset.diff` | **0 lines** — the file sets are equal |
| `changelogs-verdict.txt` | `38 files compared, 0 differing` |
| `current-changelogs/` | the current side's symmetric root: the changelog closure copied from the working tree, nothing else |
| `current-copy-verdict.txt` | `38 files copied from the working tree, 0 differing` |
| `engine-log.diff` | **0 lines** — the two engine logs agree once timestamps are stripped |
| `liquibase-update-before.norm.log`, `liquibase-update-after.norm.log` | the timestamp-normalised logs that comparison consumed |
| `metrics-before.txt`, `metrics-after.txt` | `tables 119`, `columns 1510`, `indexes 697`, `fkeys 446`, `changesets 1028` — identical files |
| `schema-before.sql`, `schema-after.sql` | the two raw dumps |
| `schema-before.norm.sql`, `schema-after.norm.sql` | the normalised dumps (provably identical to the raw ones) |
| `schema.diff` | **0 lines** |
| `checksums.txt` | one MD5, `659f521033a44a4b95e57fe0bbe105a9`, for **all four** dump files |
| `sizes.txt` | `3389 175156` for each of the four dump files |
| `create-table-count-before.txt`, `create-table-count-after.txt` | `119` and `119` |
| `dbname-occurrences-before.txt` | **`0`** — the database name never appears in the dump |
| `shared-db-before.txt`, `shared-db-after.txt` | `126` and `1065` both times, byte-identical |
| `planned-databases.txt`, `created-databases.txt` | the two composed names, and the two actually created |
| `cleanup.log` | `dropped openmrs_v3_before_eefb32d9fa1d`, `dropped openmrs_v3_after_eefb32d9fa1d` |
| `liquibase-update-before.log`, `liquibase-update-after.log` | the two engine logs, **1,169 lines each**, each with 1,028 `Running Changeset:` lines and the same **116** `Name 'PK_…' ignored for PRIMARY key.` warnings, each ending `Total change sets: 1028` and `Liquibase: Update has been successful. Rows affected: 1028` |
| `api-runtime-classpath.txt` | the resolved `api` runtime classpath the engine-version check reads |
| `base-changelogs/` | the `git archive` extraction of the base-commit changelog bytes |

One precision that matters, because the obvious worry about comparing two differently-named databases is
that a normalisation step could hide a real difference: **no normalisation was needed.** A single-database
`mysqldump --no-data --skip-comments --skip-dump-date` emits no `CREATE DATABASE` and no `USE` statement,
so the database name never appears in the output — verified as `grep -c "$DB_BEFORE" schema-before.sql`
→ **0**. The script still performs the name-normalisation substitution before diffing, but it is provably a
**no-op** here: the *raw*, un-normalised dumps already carry the identical MD5 above. Nothing in the DDL
text was rewritten, filtered or sorted.

Three safety statements, because this ran against a shared server:

- Only the two databases created for this comparison were ever written to. Their names are
  **per-run**, not per-checkout — `openmrs_v3_{before,after}_<run-id>`, where the run id is 48 bits read
  from `/dev/urandom` on each invocation. Two runs composing the same name is therefore not
  *impossible*, only **negligible** — of the order of 1 in 2.8 × 10¹⁴ for any given pair of runs — and
  the script does not rest on that arithmetic: the `CREATE DATABASE` has **no preceding `DROP` and no
  `IF NOT EXISTS`**, so a collision **fails the run closed** rather than destroying whatever holds the
  name. The recorded run used `openmrs_v3_before_eefb32d9fa1d` and `openmrs_v3_after_eefb32d9fa1d`, and
  the script wrote exactly those two names to `v3-evidence-eefb32d9fa1d/created-databases.txt` as it
  created them. Each name is regex-validated, the script refuses to start if either equals the shared
  database, and an `flock` held on an owner-only stable path prevents two runs from overlapping at all.
- The shared `openmrs` database was measured **before** the run and **after** cleanup and was identical
  both times — **126 catalogued tables / 1,065 changesets** — with the comparison asserted by `cmp`
  inside the script rather than eyeballed. "Catalogued tables" is the exact quantity the script counts:
  all `information_schema.tables` rows for that schema, which here are 125 base tables plus one view.
  A neighbouring clone's database (`openmrs_c002`) was never referenced.
- Cleanup drops **only the databases this run actually created**, and it **verifies** every release
  instead of assuming it: each `DROP` is followed by an `information_schema.schemata` count, each
  credential file is re-tested with `[ -e ]`, and the credential directory itself must be gone. Failures
  are **aggregated**, cleanup returns non-zero, and the `CLEANED` flag stays `0` so a retry remains
  possible. `PASS` is printed **only after** that verified cleanup succeeds; had anything been left
  behind, the run would have exited **78** carrying the leftover count. `INT` and `TERM` have their own
  handler that exits **130** / **143**, so an interrupted run can never be mistaken for a pass. The
  recorded run's `cleanup.log` contains exactly `dropped openmrs_v3_before_eefb32d9fa1d` and
  `dropped openmrs_v3_after_eefb32d9fa1d`; the host was then inspected independently, and no disposable
  database remained on the server and `/dev/shm` held no credential directory.

Section (e).5 below sets out, independently of this measurement, the four structural reasons the diff
**had** to be empty. The measurement and the reasoning agree.

### 2. The In-Repo Harness, and Why It Was Not Used Verbatim

A second harness exists in the repository, and it is worth being exact about its limits rather than
recommending it unqualified. The `liquibase-maven-plugin` configuration in `liquibase/pom.xml` supplies:

- `<driver>com.mysql.cj.jdbc.Driver</driver>`
- **`<url>jdbc:mysql://127.0.0.1:3306/openmrs</url>` — the SHARED database**
- `<changeLogFile>snapshots/${changelogfile}</changeLogFile>`
- `<diffTypes>${diffTypes}</diffTypes>` and `<outputChangeLogFile>snapshots/${outputChangelogfile}</outputChangeLogFile>`

That `<url>` is a hard-coded, unparameterised pointer at `openmrs`. Anyone invoking that plugin
configuration for a schema comparison **must override the URL to a disposable database** — otherwise the
run writes into whatever `openmrs` happens to be on the host, which on a shared or clinical host is
exactly the outcome to avoid. That single fact is why the executed comparison in (e).1 did **not** use the
plugin: it drove `liquibase-core` **4.32.0** directly off the `api` module's classpath with an explicit
`url` property written into each side's own defaults file, which (a) makes the target database impossible
to inherit by accident, and (b) pins both
sides to the same engine version as the library the platform actually ships, rather than to the
plugin's **4.32.0-versus-4.33.0** skew recorded as register item 7.

The same module also builds a `maven-assembly-plugin` **`jar-with-dependencies`** assembly with main
class `org.openmrs.liquibase.Main`.

### 3. Environment Notes That Matter

The containerized server in `docker-compose.yml` is **`mariadb:10.11.7`**, started with
`mariadbd --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci`. The dump therefore runs
as `mariadb-dump`, or as `mysqldump` via the compatibility symlink. `initial_test_db.sql` can be mounted
into `/docker-entrypoint-initdb.d/` by uncommenting the relevant line in `docker-compose.override.yml`,
but **a clean-database run requires that mount to stay disabled** — it is commented out by default, which
was verified.

### 4. The Procedure

> **Destructive-command warning.** This procedure creates and drops databases. Every `CREATE` and every
> `DROP` below is directed at a **name the script itself composes** from a per-run random identifier, the
> composed name is **regex-validated** before use, and the script **refuses to start** if either name
> equals the shared database. Never substitute `openmrs`, and never point it at anything holding real
> data. The shared database is read **only** for the before/after assertion in steps 5 and 11.

This is the script that produced the measurements in (e).1 — not an outline of one. Seven properties make
it safe to publish and safe to re-run:

- **Fail-fast, with the two mechanisms kept distinct.** What makes an unguarded failure fatal is
  `set -euo pipefail`, not a guard: the shell aborts on the first non-zero status, on an unset variable
  and on a failing pipeline stage. The `if ! …; then` wrappers are layered on top of that for the steps
  whose failure deserves a *specific diagnostic* — the two `CREATE DATABASE`s, the Liquibase `update`s,
  the `mysqldump`s, the metric and shared-database reads, `git archive`, the classpath resolution and the
  engine-version check. It is therefore **not** true that every fallible command carries an explicit
  guard, and this document no longer claims it does: `sed`, `md5sum`, `wc`, `mkdir`, `chmod`, `rmdir` and
  the `printf` redirections are covered by `set -e` alone and abort without a bespoke message. Three
  places deliberately suspend `-e`, each for a stated reason: the `diff` whose **non-zero status is data**
  (captured into `DIFF_STATUS` and asserted later, never discarded), the `grep -c` occurrence count that
  legitimately matches zero times (`|| true`), and the body of `cleanup`, which must attempt *every*
  release even after one of them fails. **No later step can mask an earlier failure**, and cleanup can
  only ever make the final status worse.
- **Isolation that fails closed.** Both sides live in disposable databases whose names carry a **48-bit
  per-run identifier read from the kernel CSPRNG** — *per run*, not per checkout. A collision is not
  impossible, merely **negligible** (of the order of 1 in 2.8 × 10¹⁴ per pair of runs), and correctness
  does not depend on that: creation uses a bare `CREATE DATABASE` with **no preceding `DROP` and no
  `IF NOT EXISTS`**, so an unexpected name collision **aborts the run instead of destroying whatever
  holds that name**, and each composed name is regex-validated and checked against the shared database
  first. The shared database is only ever read, and its pre/post readings are compared with `cmp` so
  drift fails the run.
- **Mutual exclusion on a verified, owner-only, stable path.** The lock is deliberately **not** derived
  from `TMPDIR`: two runs with different `TMPDIR` values would take two different locks and overlap, and
  a world-writable, non-sticky `TMPDIR` (this host's `/tmp` is exactly that — `drwxrwsrwx`, no sticky
  bit) invites a symlink swap. It lives in `${XDG_RUNTIME_DIR:-$HOME}/.openmrs-v3-schema-diff`, which is
  rejected if it exists as a non-directory, created under `umask 077`, forced to mode `700` (a `chmod`
  that fails means the directory is not yours, and the run aborts), and then re-verified as *a real
  directory, not a symlink, owned by this uid, mode 700*. The lock file itself must be absent or a
  **regular file owned by this uid** — never a symlink — and it is opened with `exec 9<>`, i.e.
  `O_RDWR|O_CREAT` and **never `O_TRUNC`**, so the descriptor cannot truncate or write anything through a
  path someone else controls. Measured after the recorded run: directory `700`, lock file mode `600` and
  **0 bytes**. The lock's scope is exactly *one concurrent run per resolved lock path*: two runs serialise
  while `XDG_RUNTIME_DIR` — or, when it is unset, `HOME` — resolves to the same directory for both, so keep
  that resolution consistent across the runs that must not overlap. That is what the comparison needs, and
  no more.
- **Credentials on a verified `tmpfs`, preferred without consulting `TMPDIR`.** An earlier revision
  preferred `CRED_DIR="${TMPDIR:-}"`, which let a caller-supplied variable send the plaintext password to
  persistent — or hostile — storage and bypassed the `tmpfs` preference entirely; that is **withdrawn**.
  The script now selects `/dev/shm` only after checking that it is a directory, **not** a symlink,
  writable, and reported as `tmpfs` by `stat -f -c %T`; the credential directory is then created fresh
  with `mktemp -d`, set to `700`, and re-verified for type, ownership and mode, with a final
  filesystem-type re-check that aborts if a directory believed to be on `tmpfs` is not. Only if no
  writable `tmpfs` exists does it fall back to `${TMPDIR:-/tmp}`, and that path **prints an explicit
  warning** that the secret may reach persistent storage and that unlinking is not erasure.
- **Cleanup that verifies its own work and cannot be papered over.** Cleanup owns a name only *after* its
  `CREATE` has succeeded. It then **verifies** each release rather than assuming it — every `DROP` is
  followed by an `information_schema.schemata` count, every credential file by `[ -e ]`, the credential
  directory by `[ -d ]` — **aggregates** every failure into a count, rebuilds `CREATED_DBS` from only the
  names that are still present so a retry stays possible, leaves `CLEANED` at `0`, and **returns
  non-zero**. An earlier revision set `CLEANED=1` on entry and discarded every `DROP`'s status, so a
  failed cleanup was silently indistinguishable from a successful one; that is **withdrawn**. `PASS` is
  now printed only after an **explicit, checked `cleanup` call**, and the `EXIT` trap is a backstop that
  may only make the status *worse*: it **overrides a zero status with 78** if anything is left behind.
- **A fresh per-run evidence directory, and a file-set check before the byte check.** Every artifact —
  both raw dumps, both normalised dumps, the `diff`, the MD5 list, the sizes, the per-side metrics, both
  Liquibase logs, the cleanup log, the classpath file and the extracted base-commit changelogs — is
  written under `./v3-evidence-<run-id>/`, created with a plain `mkdir` (**no `-p`**) after an `[ -e ]`
  refusal, so a stale directory from an earlier run cannot be silently reused or contaminate the
  evidence. Fixed scratch paths (`api_cp.txt`, `base-changelogs/`, a shared `v3-evidence/`) are
  **withdrawn** for the same reason. The changelog check now compares the **sorted file sets** of the
  base and current trees *first* and aborts on any difference, because a byte loop driven by the base
  list can only ever notice a deletion — a *newly added* current-tree changelog would pass unnoticed.
  The two changelog roots are then made **symmetric**, each holding the changelog closure and nothing
  else, with the current side's copy re-verified against the working tree by `cmp` before it is used;
  that symmetry is what turns the timestamp-normalised **engine-log comparison** into a real assertion
  about changeset execution rather than a comparison of two log formatters.
- **Signal handling that cannot report an interruption as a pass.** `INT` and `TERM` have their own
  handler, which clears the traps, cleans up once, and exits with the conventional **130** / **143**. A
  `trap cleanup EXIT INT TERM` that ends in `exit "$status"` does *not* do this: measured on this host,
  the handler observes status **0**, the body runs **twice**, and the process exits **0** — an
  interrupted schema comparison would be indistinguishable from a pass. That earlier form, and the claim
  that it "re-raises the original exit status", are **withdrawn**. The `EXIT` handler reads `$?` for one
  purpose only: to pass a *non-zero* status through unchanged while still being able to escalate a zero
  one to **78** on a failed cleanup. This was **exercised, not merely reasoned about**: a run was sent
  `SIGTERM` four seconds in, with the `before` database already created and the credential directory
  already populated. It printed `INTERRUPTED by signal 15: this run is NOT a pass`, exited **143**, and
  its `cleanup.log` recorded `dropped openmrs_v3_before_68a2f86c37c6` — after which the server held no
  disposable database, `/dev/shm` held no credential directory, the shared `openmrs` database was
  untouched, and the partial evidence directory was left in place for inspection.

**No credential appears in any argument.** Every argument of every process is world-readable through
`/proc/<pid>/cmdline`, so a same-host or same-namespace observer can lift a password out of a running
command; this was confirmed on this host rather than assumed. `mysql` and `mysqldump` therefore read a
`--defaults-extra-file`, and **Liquibase reads a `--defaultsFile`** carrying `url`, `username`,
`password`, `driver` and the changelog settings — a mechanism `liquibase.integration.commandline.Main`
supports in the 4.32.0 the platform ships, verified by running it. An earlier revision of this script
passed `--username=root --password="$DB_PASS"` to Liquibase while this prose claimed credentials never
reach the command line; the command has been corrected to match the claim. The `-u <user> -p<pw>` form
that looks natural in prose is **not executable** either: the shell reads `<user>` and `>` as
input/output redirection, so the command fails before `mysqldump` ever starts.

Both credential files are created with `umask 077` and `chmod 600`, **inside** a freshly created mode-700
directory on a verified `tmpfs`. Containment matters as much as the file mode: a directory that is new and
private means nothing can be substituted underneath either file after it is created, and it gives cleanup
something it can assert has gone — the directory itself, not merely the two names inside it. The ordering
matters more than either: the `EXIT` trap and every variable it reads are established **before `mktemp`
runs**, and the secret is written only afterwards, so at no point does credential material exist that
cleanup would not remove. The earlier revision created and populated the file first and installed the trap
several lines later, leaving a window in which an I/O failure, a shell error or an interrupt could strand
a plaintext password on disk. Placing the files on a **`tmpfs`** (`/dev/shm`, *validated* as one rather
than assumed to be one) is what keeps the secret off persistent storage altogether, and the fallback that
cannot do so says so out loud.

> **Note:** `rm -f` **unlinks** the file; that is **not** erasure. `shred` is best-effort at best and
> offers **no portable guarantee** — not on SSD or other flash translation layers, not on copy-on-write
> or journaling filesystems, and not where snapshots or backups exist. The reliable mitigations are the
> two this script uses: keep the secret off persistent storage (`tmpfs`), and scope it to a credential
> that is disposable. An earlier revision of this document recommended substituting `shred -u` "if a
> shredding guarantee is required"; that advice was wrong and is **withdrawn**.

Save the block below to a file and run it; it is the script, not an excerpt of one. Its
`#!/usr/bin/env bash` line sits **flush left on purpose** — the kernel honours an interpreter directive
only when the `#!` occupies the first two bytes of the file — while the two-space indentation on the
remaining lines is this document's own formatting, which the shell ignores.

```bash
#!/usr/bin/env bash
  set -euo pipefail

  BASE_COMMIT=3934d8086c684269e935562f25e806be91947115
  DB_HOST=127.0.0.1; DB_PORT=3306
  SHARED_DB=openmrs                           # READ-ONLY here. Never a DROP/CREATE target.
  : "${DB_PASS:?export DB_PASS before running; no credential is written into this document}"
  : "${HOME:?HOME must be set: the lock lives in a stable owner-only directory, not in TMPDIR}"

  # 0. Per-run identity FIRST: both disposable database names, the evidence directory and the
  #    credential directory take their names from it. The lock in step 2 deliberately does NOT -
  #    it has to be stable, because two runs can only contend for a path they both compute the
  #    same way, so it is derived from the environment instead.
  #    48 bits from the kernel CSPRNG. A collision is not impossible, merely negligible
  #    (~1 in 2.8e14 per pair of runs), and every use of the value is fail-closed anyway:
  #    the CREATE has no preceding DROP, the evidence directory is created with a plain
  #    mkdir, and the lock is advisory-locked - so a collision aborts rather than destroys.
  RUN_ID="$(od -An -N6 -tx1 /dev/urandom | tr -d ' \n')"
  DB_BEFORE="openmrs_v3_before_$RUN_ID"
  DB_AFTER="openmrs_v3_after_$RUN_ID"
  for d in "$DB_BEFORE" "$DB_AFTER"; do
    if [ "$d" = "$SHARED_DB" ] || ! [[ "$d" =~ ^openmrs_v3_(before|after)_[0-9a-f]{12}$ ]]; then
      printf 'FATAL: refusing to operate on database name %s\n' "$d" >&2; exit 1
    fi
  done

  # 1. Declare everything cleanup owns, then install the traps - BEFORE any secret, any
  #    directory and any database exists. There is no window in which either is unowned.
  MYSQL_CNF=; LB_CNF=; CRED_DIR=; CREATED_DBS=(); CLEANED=0; CLEANUP_FAILURES=0
  CLEANUP_LOG=                                # a real file once step 4 has run; empty before that

  # Diagnostics must never depend on a log file existing: cleanup can run before step 4, and
  # `>> /dev/stderr` is not portable (it fails outright when fd 2 is a socket). Messages go to
  # fd 2 directly until there is a file, and command output goes to /dev/null until then.
  cl_log() {
    if [ -n "$CLEANUP_LOG" ]; then printf '%s\n' "$*" >> "$CLEANUP_LOG"; else printf '%s\n' "$*" >&2; fi
  }

  # A private directory means: a real directory, not a symlink, owned by this uid, mode 700.
  verify_private_dir() {
    local p="$1" info
    if [ -L "$p" ]; then printf 'FATAL: %s is a symlink\n' "$p" >&2; return 1; fi
    if ! info="$(stat -c '%F %u %a' "$p" 2>/dev/null)"; then
      printf 'FATAL: cannot stat %s\n' "$p" >&2; return 1
    fi
    if [ "$info" != "directory $(id -u) 700" ]; then
      printf 'FATAL: %s is not a directory owned by uid %s with mode 700 (got: %s)\n' \
        "$p" "$(id -u)" "$info" >&2
      return 1
    fi
  }

  # Cleanup is idempotent, aggregates every failure, verifies every release, keeps retry
  # state, and returns NON-ZERO if anything is left behind. It never inspects or alters $?.
  cleanup() {
    if [ "$CLEANED" -eq 1 ]; then return 0; fi
    local db f present failures=0
    local -a remaining=()
    set +e
    if [ "${#CREATED_DBS[@]}" -gt 0 ] && [ -n "$MYSQL_CNF" ] && [ -f "$MYSQL_CNF" ]; then
      for db in "${CREATED_DBS[@]}"; do
        mysql --defaults-extra-file="$MYSQL_CNF" -e "DROP DATABASE IF EXISTS \`$db\`;" \
          >> "${CLEANUP_LOG:-/dev/null}" 2>&1
        present="$(mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e \
          "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='$db';" \
          2>> "${CLEANUP_LOG:-/dev/null}")"
        if [ "$present" = 0 ]; then
          cl_log "dropped $db"
        else
          cl_log "FAILED to drop $db (still present: ${present:-unknown})"
          remaining+=("$db"); failures=$((failures + 1))
        fi
      done
    elif [ "${#CREATED_DBS[@]}" -gt 0 ]; then
      cl_log "FAILED: no usable credential file; ${#CREATED_DBS[@]} database(s) cannot be dropped"
      remaining=("${CREATED_DBS[@]}"); failures=$((failures + 1))
    fi
    CREATED_DBS=(${remaining[@]+"${remaining[@]}"})   # only the undropped names survive
    for f in "$MYSQL_CNF" "$LB_CNF"; do
      [ -n "$f" ] || continue
      rm -f -- "$f"                                  # unlink, NOT erasure - see the note above
      if [ -e "$f" ]; then
        cl_log "FAILED to remove $f"; failures=$((failures + 1))
      fi
    done
    if [ -n "$CRED_DIR" ] && [ -d "$CRED_DIR" ]; then
      rmdir -- "$CRED_DIR" 2>> "${CLEANUP_LOG:-/dev/null}"
      if [ -d "$CRED_DIR" ]; then
        cl_log "FAILED to remove $CRED_DIR"; failures=$((failures + 1))
      fi
    fi
    set -e
    CLEANUP_FAILURES="$failures"
    if [ "$failures" -ne 0 ]; then return 1; fi      # CLEANED stays 0, so a retry is possible
    CLEANED=1
    return 0
  }

  on_exit() {                 # EXIT: last resort. It may only make the status WORSE, never better.
    local status=$?
    if ! cleanup; then
      printf 'FATAL: cleanup left %s resource(s) behind - see %s. This run is NOT a pass.\n' \
        "$CLEANUP_FAILURES" "${CLEANUP_LOG:-the messages above}" >&2
      exit 78                                        # overrides a zero status
    fi
    exit "$status"
  }

  on_signal() {               # INT/TERM: clean up once, then exit 128+signo - never 0
    local signo="$1"
    trap - EXIT INT TERM
    if ! cleanup; then
      printf 'WARNING: cleanup left %s resource(s) behind - see %s\n' \
        "$CLEANUP_FAILURES" "${CLEANUP_LOG:-the messages above}" >&2
    fi
    printf 'INTERRUPTED by signal %s: this run is NOT a pass\n' "$signo" >&2
    exit "$((128 + signo))"
  }

  trap on_exit EXIT
  trap 'on_signal 2'  INT
  trap 'on_signal 15' TERM

  # 2. Mutual exclusion on a STABLE, owner-only path. The lock is deliberately NOT derived
  #    from TMPDIR: two runs with different TMPDIR values would otherwise take two different
  #    locks and overlap. Scope: one concurrent run per RESOLVED LOCK PATH - runs serialise
  #    only while XDG_RUNTIME_DIR (or HOME, when it is unset) resolves to the same directory
  #    for each of them, so keep that resolution consistent across the runs that must not
  #    overlap. The file is opened read-write with O_CREAT and WITHOUT O_TRUNC, so nothing is
  #    ever written or destroyed through it, and the directory is verified owner-only first,
  #    so no other user can plant a symlink in it.
  umask 077                                   # every file and directory below is private
  LOCK_DIR="${XDG_RUNTIME_DIR:-$HOME}/.openmrs-v3-schema-diff"
  if [ -e "$LOCK_DIR" ] && [ ! -d "$LOCK_DIR" ]; then
    printf 'FATAL: %s exists and is not a directory\n' "$LOCK_DIR" >&2; exit 1
  fi
  if [ ! -d "$LOCK_DIR" ]; then
    ( umask 077; mkdir "$LOCK_DIR" )          # umask, not `mkdir -p -m`, which only modes the leaf
  fi
  if ! chmod 700 "$LOCK_DIR" 2>/dev/null; then
    printf 'FATAL: cannot set mode 700 on %s - it is not yours\n' "$LOCK_DIR" >&2; exit 1
  fi
  verify_private_dir "$LOCK_DIR"
  LOCK_FILE="$LOCK_DIR/schema-diff.lock"
  if [ -L "$LOCK_FILE" ]; then
    printf 'FATAL: %s is a symlink\n' "$LOCK_FILE" >&2; exit 1
  fi
  if [ -e "$LOCK_FILE" ]; then
    if [ ! -f "$LOCK_FILE" ] || [ "$(stat -c %u "$LOCK_FILE")" != "$(id -u)" ]; then
      printf 'FATAL: %s is not a regular file owned by uid %s\n' "$LOCK_FILE" "$(id -u)" >&2; exit 1
    fi
  fi
  exec 9<>"$LOCK_FILE"                        # O_RDWR|O_CREAT - never O_TRUNC
  if ! flock -n 9; then
    printf 'FATAL: another comparison run holds %s\n' "$LOCK_FILE" >&2; exit 1
  fi

  # 3. Credentials: a FRESH private directory, PREFERRING a verified tmpfs. /dev/shm is used
  #    only after it checks out as a writable, non-symlink directory that stat reports as
  #    tmpfs, and TMPDIR is not consulted for that preferred path, so no caller-supplied
  #    variable can redirect the secret to hostile storage. Only when no such tmpfs exists
  #    does the parent fall back to ${TMPDIR:-/tmp}, and that branch WARNS that the secret
  #    may reach persistent storage and that unlinking is not erasure. Because the directory
  #    is new and mode 700, nothing can be substituted underneath the files once created.
  CRED_PARENT=
  CRED_PERSISTENT=no
  if [ -d /dev/shm ] && [ ! -L /dev/shm ] && [ -w /dev/shm ] \
     && [ "$(stat -f -c %T /dev/shm 2>/dev/null)" = tmpfs ]; then
    CRED_PARENT=/dev/shm
  else
    CRED_PARENT="${TMPDIR:-/tmp}"
    CRED_PERSISTENT=yes
    printf 'WARNING: no writable tmpfs available; the credential files will be created under %s, which may be persistent storage. Unlinking is not erasure.\n' \
      "$CRED_PARENT" >&2
  fi
  CRED_DIR="$(mktemp -d "$CRED_PARENT/openmrs-v3-cred-$RUN_ID.XXXXXXXX")"
  chmod 700 "$CRED_DIR"
  verify_private_dir "$CRED_DIR"
  CRED_FS="$(stat -f -c %T "$CRED_DIR")"
  if [ "$CRED_PERSISTENT" = no ] && [ "$CRED_FS" != tmpfs ]; then
    printf 'FATAL: %s is not on a tmpfs (got %s)\n' "$CRED_DIR" "$CRED_FS" >&2; exit 1
  fi
  MYSQL_CNF="$CRED_DIR/mysql.cnf"
  LB_CNF="$CRED_DIR/liquibase.properties"
  : > "$MYSQL_CNF"; : > "$LB_CNF"
  chmod 600 "$MYSQL_CNF" "$LB_CNF"
  printf '[client]\nuser=root\npassword=%s\nhost=%s\nport=%s\n' \
    "$DB_PASS" "$DB_HOST" "$DB_PORT" > "$MYSQL_CNF"

  # 4. A FRESH, per-run, private evidence directory. Plain `mkdir` - no -p - so a stale
  #    directory from an earlier run cannot contaminate this one, and every retained artifact
  #    and every working-tree scratch file lives inside it rather than in fixed working-tree
  #    paths. Two pieces of state stay outside it by design: the credential directory from
  #    step 3, which belongs on tmpfs and is removed by cleanup, and the stable lock from
  #    step 2, which several runs must be able to find.
  EVIDENCE="./v3-evidence-$RUN_ID"
  if [ -e "$EVIDENCE" ]; then
    printf 'FATAL: %s already exists; refusing to reuse an evidence directory\n' "$EVIDENCE" >&2
    exit 1
  fi
  mkdir "$EVIDENCE"
  chmod 700 "$EVIDENCE"
  CLEANUP_LOG="$EVIDENCE/cleanup.log"
  : > "$CLEANUP_LOG"
  printf '%s\n' "$DB_BEFORE" "$DB_AFTER" > "$EVIDENCE/planned-databases.txt"
  : > "$EVIDENCE/created-databases.txt"       # what this run actually created, appended as it goes

  # 5. Shared database pre-state - READ ONLY. Re-checked identical in step 10.
  if ! mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e \
    "SELECT (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$SHARED_DB'), \
            (SELECT COUNT(*) FROM $SHARED_DB.liquibasechangelog);" > "$EVIDENCE/shared-db-before.txt"
  then printf 'FATAL: cannot read the shared database pre-state\n' >&2; exit 1; fi

  # 6. Materialise the BASE-COMMIT changelog bytes without touching the working tree.
  mkdir "$EVIDENCE/base-changelogs"
  BASE_ROOT="$EVIDENCE/base-changelogs/api/src/main/resources"
  if ! git archive "$BASE_COMMIT" -- 'api/src/main/resources/liquibase-*.xml' \
                                     'api/src/main/resources/org/openmrs/liquibase' \
       | tar -x -C "$EVIDENCE/base-changelogs"
  then printf 'FATAL: could not extract the base-commit changelogs\n' >&2; exit 1; fi

  # 6a. FILE-SET equality FIRST. A base-only deletion and a current-only ADDITION are both
  #     failures, and a byte loop driven by the base list can only ever see the former.
  ( cd "$BASE_ROOT" && find . -name 'liquibase-*.xml' -type f | LC_ALL=C sort ) \
    > "$EVIDENCE/changelogs-base.txt"
  ( cd api/src/main/resources && find . -name 'liquibase-*.xml' -type f | LC_ALL=C sort ) \
    > "$EVIDENCE/changelogs-current.txt"
  if ! diff -u "$EVIDENCE/changelogs-base.txt" "$EVIDENCE/changelogs-current.txt" \
       > "$EVIDENCE/changelogs-fileset.diff"
  then printf 'FATAL: the changelog FILE SET differs from the base commit\n' >&2; exit 1; fi

  # 6b. ...then byte equality over that identical set.
  diffcount=0
  while IFS= read -r rel; do
    if ! cmp -s "$BASE_ROOT/$rel" "api/src/main/resources/$rel"; then
      printf 'DIFFERS: %s\n' "$rel" >> "$EVIDENCE/changelogs-bytes.txt"
      diffcount=$((diffcount + 1))
    fi
  done < "$EVIDENCE/changelogs-base.txt"
  CHANGELOG_COUNT="$(wc -l < "$EVIDENCE/changelogs-base.txt" | tr -d ' ')"
  if [ "$diffcount" -ne 0 ]; then
    printf 'FATAL: changelog bytes are not frozen (%s differing)\n' "$diffcount" >&2; exit 1
  fi
  printf '%s files compared, 0 differing\n' "$CHANGELOG_COUNT" > "$EVIDENCE/changelogs-verdict.txt"

  # 6c. SYMMETRY. Give the current side a root holding the changelog closure and NOTHING else,
  #     because api/src/main/resources also carries META-INF/services registrations for
  #     liquibase.logging.LogService, liquibase.change.Change, liquibase.sqlgenerator.SqlGenerator
  #     and liquibase.datatype.LiquibaseDataType whose implementing classes live in api's own
  #     output, not on this dependency-only classpath. Passing that whole root made ONE side
  #     raise java.util.ServiceConfigurationError and fall back to java.util.logging, so the two
  #     sides differed in more than the changelog bytes under comparison. Now they do not:
  #     both roots hold exactly `liquibase-*.xml` plus `org/openmrs/liquibase/**`.
  #     `mkdir -p` is used only for nested paths INSIDE the freshly created evidence directory.
  CURRENT_ROOT="$EVIDENCE/current-changelogs/api/src/main/resources"
  mkdir -p "$CURRENT_ROOT"
  if ! ( cd api/src/main/resources && tar -cf - liquibase-*.xml org/openmrs/liquibase ) \
       | tar -x -C "$CURRENT_ROOT"
  then printf 'FATAL: could not materialise the current changelog closure\n' >&2; exit 1; fi

  # The copy is only usable as evidence if it is provably the working tree's bytes.
  copydiff=0
  while IFS= read -r rel; do
    if ! cmp -s "$CURRENT_ROOT/$rel" "api/src/main/resources/$rel"; then
      printf 'DIFFERS: %s\n' "$rel" >> "$EVIDENCE/current-copy-bytes.txt"
      copydiff=$((copydiff + 1))
    fi
  done < "$EVIDENCE/changelogs-current.txt"
  if [ "$copydiff" -ne 0 ]; then
    printf 'FATAL: the current-side copy is not byte-identical to the working tree (%s differing)\n' \
      "$copydiff" >&2; exit 1
  fi
  printf '%s files copied from the working tree, 0 differing\n' "$CHANGELOG_COUNT" \
    > "$EVIDENCE/current-copy-verdict.txt"

  # 7. Resolve the SAME engine for both sides: liquibase-core 4.32.0 off the api classpath.
  #    mdep.outputFile MUST be absolute: with -pl it is otherwise resolved against the
  #    module basedir and lands in api/, not here.
  CP_FILE="$PWD/${EVIDENCE#./}/api-runtime-classpath.txt"
  if ! ./mvnw -B -q -o -pl api dependency:build-classpath \
       -Dmdep.outputFile="$CP_FILE" -Dmdep.includeScope=runtime
  then printf 'FATAL: could not resolve the api runtime classpath\n' >&2; exit 1; fi
  if ! grep -q 'liquibase-core/4\.32\.0/' "$CP_FILE"; then
    printf 'FATAL: wrong Liquibase engine on the resolved classpath\n' >&2; exit 1
  fi
  CP="$(cat "$CP_FILE")"

  # 8-9. Install and dump each side. $1 = database, $2 = changelog resource root, $3 = label.
  run_side() {
    local db="$1" root="$2" label="$3"

    # Fail CLOSED: no DROP first and no IF NOT EXISTS, so an unexpected name collision
    # aborts the run instead of destroying whatever already holds that name.
    if ! mysql --defaults-extra-file="$MYSQL_CNF" -e \
      "CREATE DATABASE \`$db\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    then printf 'FATAL: could not create %s - a name collision aborts the run\n' "$db" >&2; exit 1; fi
    CREATED_DBS+=("$db")     # only now, having created it, does cleanup own this name
    printf '%s\n' "$db" >> "$EVIDENCE/created-databases.txt"   # auditable: what this run owns

    # Credentials AND the target URL are PROPERTIES IN THE MODE-600 DEFAULTS FILE, never
    # command-line options: every argument is world-readable through /proc/<pid>/cmdline.
    # The url property is also what keeps liquibase/pom.xml's shared-database <url> from
    # being inherited. The changelog table names match DatabaseUpdater.
    printf 'url=jdbc:mysql://%s:%s/%s\nusername=root\npassword=%s\ndriver=com.mysql.cj.jdbc.Driver\nchangeLogFile=liquibase-schema-only.xml\ndatabaseChangelogTableName=liquibasechangelog\ndatabaseChangelogLockTableName=liquibasechangeloglock\nlogLevel=warning\n' \
      "$DB_HOST" "$DB_PORT" "$db" "$DB_PASS" > "$LB_CNF"

    if ! java -cp "$root:$CP" liquibase.integration.commandline.Main \
        --defaultsFile="$LB_CNF" update > "$EVIDENCE/liquibase-update-$label.log" 2>&1
    then printf 'FATAL: liquibase update failed for %s\n' "$label" >&2; exit 1; fi

    if ! mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e "
      SELECT 'tables',      COUNT(*) FROM information_schema.tables  WHERE table_schema='$db' AND table_type='BASE TABLE'
      UNION ALL SELECT 'columns',    COUNT(*) FROM information_schema.columns WHERE table_schema='$db'
      UNION ALL SELECT 'indexes',    COUNT(DISTINCT CONCAT(table_name,'.',index_name)) FROM information_schema.statistics WHERE table_schema='$db'
      UNION ALL SELECT 'fkeys',      COUNT(*) FROM information_schema.table_constraints WHERE table_schema='$db' AND constraint_type='FOREIGN KEY'
      UNION ALL SELECT 'changesets', COUNT(*) FROM \`$db\`.liquibasechangelog;" > "$EVIDENCE/metrics-$label.txt"
    then printf 'FATAL: could not read metrics for %s\n' "$db" >&2; exit 1; fi

    if ! mysqldump --defaults-extra-file="$MYSQL_CNF" --no-data --skip-comments --skip-dump-date \
        "$db" > "$EVIDENCE/schema-$label.sql"
    then printf 'FATAL: mysqldump failed for %s\n' "$db" >&2; exit 1; fi
  }
  run_side "$DB_BEFORE" "$BASE_ROOT"     before      # base-commit changelog bytes
  run_side "$DB_AFTER"  "$CURRENT_ROOT" after       # working-tree changelog bytes, same closure

  # 10. Compare. The name substitution is a documented NO-OP for a single-database dump
  #     (no CREATE DATABASE / USE is emitted); it is kept only so the step is explicit.
  sed "s/\`$DB_BEFORE\`/\`DBNAME\`/g" "$EVIDENCE/schema-before.sql" > "$EVIDENCE/schema-before.norm.sql"
  sed "s/\`$DB_AFTER\`/\`DBNAME\`/g"  "$EVIDENCE/schema-after.sql"  > "$EVIDENCE/schema-after.norm.sql"
  set +e
  diff "$EVIDENCE/schema-before.norm.sql" "$EVIDENCE/schema-after.norm.sql" > "$EVIDENCE/schema.diff"
  DIFF_STATUS=$?          # captured, asserted in step 12 - never discarded
  set -e
  md5sum "$EVIDENCE"/schema-*.sql > "$EVIDENCE/checksums.txt"
  wc -l -c "$EVIDENCE"/schema-*.sql > "$EVIDENCE/sizes.txt"
  grep -c "^CREATE TABLE" "$EVIDENCE/schema-before.sql" > "$EVIDENCE/create-table-count-before.txt"
  grep -c "^CREATE TABLE" "$EVIDENCE/schema-after.sql"  > "$EVIDENCE/create-table-count-after.txt"
  grep -c "$DB_BEFORE" "$EVIDENCE/schema-before.sql" > "$EVIDENCE/dbname-occurrences-before.txt" || true

  # 10a. The two engine logs must agree once timestamps are stripped: the same changesets in the
  #      same order, with the same warnings. This is only a meaningful assertion because step 6c
  #      made the two roots symmetric - otherwise it would compare two different log formatters.
  for label in before after; do
    sed -E -e 's/^\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\] //' \
           -e 's/Starting Liquibase at [0-9]{2}:[0-9]{2}:[0-9]{2}/Starting Liquibase at HH:MM:SS/' \
      "$EVIDENCE/liquibase-update-$label.log" > "$EVIDENCE/liquibase-update-$label.norm.log"
  done
  if ! diff "$EVIDENCE/liquibase-update-before.norm.log" \
            "$EVIDENCE/liquibase-update-after.norm.log" > "$EVIDENCE/engine-log.diff"
  then
    printf 'FATAL: the two engine logs differ beyond timestamps - see %s\n' \
      "$EVIDENCE/engine-log.diff" >&2; exit 1
  fi

  # 11. Shared database post-state must equal the pre-state. Asserted, not eyeballed.
  if ! mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e \
    "SELECT (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$SHARED_DB'), \
            (SELECT COUNT(*) FROM $SHARED_DB.liquibasechangelog);" > "$EVIDENCE/shared-db-after.txt"
  then printf 'FATAL: cannot read the shared database post-state\n' >&2; exit 1; fi
  if ! cmp -s "$EVIDENCE/shared-db-before.txt" "$EVIDENCE/shared-db-after.txt"; then
    printf 'FATAL: the shared database changed\n' >&2; exit 1
  fi

  # 12. Verdict from the CAPTURED status - and only after cleanup has been VERIFIED, because
  #     a PASS printed while a disposable database or a credential file still existed would
  #     be a false statement. The EXIT trap remains installed as a backstop.
  if [ "$DIFF_STATUS" -ne 0 ]; then
    printf 'FAIL: schemas differ - see %s\n' "$EVIDENCE/schema.diff" >&2; exit 1
  fi
  if ! cleanup; then
    printf 'FATAL: schemas matched, but cleanup left %s resource(s) behind. NOT a pass.\n' \
      "$CLEANUP_FAILURES" >&2
    exit 78
  fi
  printf 'PASS: schemas identical, and every resource this run created was released\n'
  printf 'evidence: %s\n' "$EVIDENCE"
```

`$DB_PASS` is supplied by the operator from the environment (the containerised development server in
`docker-compose.yml` uses the project's default), so no credential is written into this document. The
script refuses to start without it — `: "${DB_PASS:?…}"` — rather than silently attempting a passwordless
connection.

The run leaves **exactly one** byproduct in the working tree: the per-run directory
`./v3-evidence-<run-id>/`, which holds every artifact — including the extracted base-commit changelogs, the
symmetric copy of the current-side changelogs and the resolved classpath, which earlier revisions scattered
into the fixed paths `base-changelogs/` and `api_cp.txt`. The recorded run's directory measures **15 MB**,
and the bulk of it is the two changelog trees at **6.8 MB each** — the dumps and engine logs together
account for only **1.3 MB**. It does not belong in a commit, so delete it (or add `v3-evidence-*` to a
local exclude)
once the evidence has been read. Outside the working tree the run leaves one thing more: the owner-only
lock directory `${XDG_RUNTIME_DIR:-$HOME}/.openmrs-v3-schema-diff` holding a **zero-byte, mode-600** lock
file. **Leave both where they are**: the file holds no data — it is never written to, only `flock`ed —
and removing it while another run is in flight would break the mutual exclusion it exists to provide.

Nothing else survives the run. The credential directory and both credential files are removed by the
verified cleanup, and the two disposable databases are dropped and then confirmed absent; if any of that
had failed, the script would have exited **78** instead of printing `PASS`.

### 5. Why the Diff Is Empty by Construction

Four independent reasons, each verified:

**Reason 1 — every changelog file is frozen byte-for-byte.** `git diff --name-status "$BASE"..HEAD`
against the base commit reports
**zero** changed paths for `api/src/main/resources/liquibase-*.xml` and **zero** for everything under
`api/src/main/resources/org/openmrs/liquibase/`. No changeset, checksum or ordering changed.

The frozen set is **38 files**, and the arithmetic deserves care because it is easy to get wrong:

| Location | Count |
|---|---|
| Top-level `api/src/main/resources/liquibase-*.xml` | **6** |
| `api/src/main/resources/org/openmrs/liquibase/snapshots/core-data` | 10 |
| `api/src/main/resources/org/openmrs/liquibase/snapshots/schema-only` | 10 |
| `api/src/main/resources/org/openmrs/liquibase/updates` | 12 |
| Subtotal under `org/openmrs/liquibase/` | **32** |
| **TOTAL frozen changelog files** | **38** |

The six top-level files are `liquibase-core-data.xml`, `liquibase-empty-changelog.xml`,
`liquibase-schema-only.xml`, `liquibase-update-to-latest-from-1.9.x.xml`,
`liquibase-update-to-latest-template.xml` and `liquibase-update-to-latest.xml`.

> **Note:** the figure recorded during planning described this set as "38 + 32", adding the two to reach
> 70. That **double-counts**. The 38 is the result of a *recursive* `find`, so it already contains the 32
> nested files. Verified here: `find api/src/main -name 'liquibase-*.xml' | wc -l` gives **38**;
> `ls api/src/main/resources/liquibase-*.xml | wc -l` gives **6**; and
> `find api/src/main/resources/org/openmrs/liquibase -name 'liquibase-*.xml' | wc -l` gives **32**,
> proving all 32 nested files match the same glob the recursive count used. The correct total is
> **6 + 32 = 38**.

**Reason 2 — no mapping was modified.** All **20** `.hbm.xml` files remain as they are, 17 of them under
`org/openmrs/api/db/hibernate/`, and `git diff` reports zero changed `.hbm.xml` paths. No `@Entity`
annotation was touched. `hibernate.cfg.xml` is unchanged and keeps its **20** `<mapping resource>`
entries plus **2** `<mapping class>` entries — `org.openmrs.ObsReferenceRange` and
`org.openmrs.ConceptReferenceRange` — along with its Jakarta property keys
`jakarta.persistence.validation.mode=none` and `jakarta.persistence.sharedCache.mode=ENABLE_SELECTIVE`.

**Reason 3 — the `liquibase-core` library version does not move.** Held at **4.32.0**, so no
engine-level generation difference can arise.

**Reason 4 — `StringEnumType`'s column representation is provably unchanged.** Verified line by line in
`api/src/main/java/org/openmrs/api/db/hibernate/type/StringEnumType.java`: `getSqlType()` returns
`Types.VARCHAR`; `nullSafeGet` reads `rs.getString(position)` behind an explicit `rs.wasNull()` guard and
then `Enum.valueOf(enumClass, name)`; `nullSafeSet` writes `st.setString(index, value.name())` or
`st.setNull(index, Types.VARCHAR)`; and `disassemble`/`assemble` round-trip the enum **name string**. It
implements `EnhancedUserType<Enum>, DynamicParameterizedType` — already the Hibernate 6/7 contract, and
the sanctioned replacement for the removed `org.hibernate.type.EnumType`. The only edit to that file in
this change is a comment.

**`StringEnumType` has four consumers, not three.** An earlier revision of this document counted only the
HBM sites and said "exactly three", which is wrong: the type is also applied by annotation. The full,
re-measured inventory — `git grep StringEnumType` across `*.hbm.xml` and `*.java` under `api/src/main`,
excluding the type's own file:

| # | Consumer | Site | Mapped column (live type) | Enum parameter |
|---|---|---|---|---|
| 1 | `Obs.hbm.xml` | line 67, `<type name="…StringEnumType">` | `obs.status` — `varchar(16)` | `org.openmrs.Obs$Status` |
| 2 | `Obs.hbm.xml` | line 72, `<type name="…StringEnumType">` | `obs.interpretation` — `varchar(32)` | `org.openmrs.Obs$Interpretation` |
| 3 | `OrderSet.hbm.xml` | line 31, `<type name="…StringEnumType">` | `order_set.operator` — `varchar(50)` | `org.openmrs.OrderSet$Operator` |
| 4 | **`ConceptName.java`** | line 105, `@Type(value = StringEnumType.class, parameters = { @Parameter(name = "enumClass", value = "org.openmrs.api.ConceptNameType") })`, import at line 43 | `concept_name.concept_name_type` — `varchar(50)` | `org.openmrs.api.ConceptNameType` |

The live column types are read from `information_schema.columns` on the installed schema, not inferred from
the mapping metadata.

The fourth consumer is the one that matters for reasoning about this type's future, and it is why the
type's own javadoc carries a Rule 5 note (register item 5): `ConceptName` is **already annotation-mapped**
— `@Entity` on the class — yet it still applies `StringEnumType` through `@Type`. So the "delete this
class once every consumer is annotation-mapped" precondition in that javadoc is **partly satisfied
already**, and completing the HBM migration for `Obs` and `OrderSet` would still not make the type
removable. Three HBM sites plus one annotated site is also why the column-parity argument above is
load-bearing for **four** columns rather than three; all four are `VARCHAR` columns holding the enum
**name**, read and written by the same code path.

> **Note:** the `liquibase/` module is **not** where the changelogs live. It declares **no
> `liquibase-core` dependency at all**, and its four source files — `AbstractSnapshotTuner`,
> `CoreDataTuner`, `Main` and `SchemaOnlyTuner` — contain **zero `import liquibase.*`** statements. It is
> a dom4j-based changelog-XML tuner, untouched by both the exclusion and by any Liquibase version
> decision. The changelogs themselves live in `api/src/main/resources`.

### 6. Profiles Not Executed

**Only the default `test` phase was executed.** The `integration-test` and `performance-test` profiles are
recorded with their invocations and expectations but **were not run**:

```bash
  ./mvnw verify -Pintegration-test -Pskip-default-test -B   # documented, NOT executed here
  ./mvnw verify -Pperformance-test -Pskip-default-test -B   # documented, NOT executed here
```

`-Pskip-default-test` is not optional decoration. Each of those profiles *adds* a Surefire execution bound
to the `test` phase (`**/*IT.java` and `**/*DatabaseIT.java` for the first, `**/*PerformanceIT.java` for
the second) without disabling the `default-test` execution, so omitting it re-runs the entire 5,106-test
default suite ahead of the integration or performance tests. `skip-default-test` sets `skipTests` on the
`default-test` execution alone, which is what leaves only the profile's own tests running.

## Section (f): Test-Infrastructure Adaptations

**Result: zero test-infrastructure adaptations were necessary.**

The requirement asked that every adapted test-infrastructure change be documented together with the
removed-API reason that forced it. Because **no removed API is in use anywhere in the test tree**, that
obligation is discharged by this affirmative, evidenced statement rather than by an omission.

The evidence, all verified:

- `api/src/test/java/org/openmrs/test/jupiter/BaseContextSensitiveNonTransactionalTest.java` is **1,045
  lines** and already imports **only** Jakarta, Spring 7 and DBUnit 3 APIs.
- The harness runs on **DBUnit 3.0.0**, **JUnit Jupiter 6.0.3**, **Mockito 5.23.0**, **Spring Test
  7.0.7** and **H2 2.3.232**, with `SpringExtension` and `MockitoExtension`.
- `Environment.DIALECT` is set to `H2Dialect`, and the URL is
  `jdbc:h2:mem:openmrs;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;MODE=LEGACY;NON_KEYWORDS=VALUE;IGNORECASE=TRUE`,
  switchable to a real server through `useInMemoryDatabase()` with `-DuseInMemoryDatabase=false`.
- The fixed reference datasets `org/openmrs/include/initialInMemoryTestDataSet.xml` and
  `org/openmrs/include/standardTestDataset.xml` are loaded via `executeDataSet(...)`.

Consequently **zero test files appear in the change set**. The 5,106-test run against those fixed
datasets *is* the service-output capture for read, validation and privilege behaviour; no new harness was
built, and none was needed.

## Change Inventory

Every edit below carries a **named, per-edit attribution** — but of two distinct kinds, which the blanket
wording of an earlier revision ("every edit … to the Spring 6+/Hibernate 6+/Jakarta/Java 21 target")
flattened into one and thereby overstated. The migration edits, sections 1 through 5, each cite the target
generation that Rule 1 and TR1 require. The five comment-only annotations in section 6 cite **Rule 5**
instead, and must: Rule 5 exists precisely for defects that are *not* attributable to the target stack, so
asserting a target-stack attribution for them would misstate why they were touched at all. The whitespace
restorations recorded in section 3 cite Rule 1 in its negative form — they remove a change that had no
attribution. Nineteen files were in the UPDATE set and one file — this document — was created.

> **Note:** of those nineteen, **eighteen were actually modified**; the nineteenth, the root `pom.xml`,
> was **verify-only** and therefore has an empty diff. Its inspection confirmed
> `maven.compiler.release` 21, the provided-scope `jakarta.servlet-api` 6.1.0 override, the spotless
> import order and the enforcer's `requireMavenVersion` floor, with no version moved.

### 1. Dependency-Graph Hygiene

- `bom/pom.xml` — the single `<exclusion>` for `javax.xml.bind:jaxb-api`, added to the existing managed
  `liquibase-core` exclusions block. *Attribution: the namespace goal itself (Rule 3, TR4).*
- `api/pom.xml`, `web/pom.xml` — verified that `liquibase-core` stays **version-less and
  exclusion-less** so the BOM-managed exclusions apply. *Attribution: TR3, the single version control
  point; this is load-bearing, not cosmetic.*

### 2. Dead-Coordinate Removal

- `web/pom.xml` — removed `commons-fileupload:commons-fileupload` and
  `org.apache.commons:commons-fileupload2-jakarta-servlet6`. *Attribution: a javax.servlet-generation
  API surface and its unused non-GA successor, both superseded by Spring's
  `StandardServletMultipartResolver`.*
- `bom/pom.xml` — removed both managed fileupload blocks and their two version properties, plus the
  managed `groovy-all` block and its version property. *Attribution: TR5 coupled edits.*
- `api/pom.xml` — removed `org.codehaus.groovy:groovy-all`. *Attribution: a pre-Java-9 artifact with
  zero source usage.*
- `tools/pom.xml` — deleted the two dead JDK-8 profiles `default-tools.jar` and `mac-tools.jar`.
  *Attribution: `tools.jar` does not exist in JDK 21, so the activations can never fire.*
- `NOTICE.md` — removed the three attribution lines for the removed coordinates. *Attribution: TR5;
  the project must not attribute a library it no longer ships.*

### 3. Spring Configuration Modernization

Five contexts moved to **versionless** XSD references. Contexts stay XML — XML-to-Java configuration
conversion is explicitly **excluded** — and only the `xsi:schemaLocation` attribute changed. That last clause
is a measured statement rather than an assurance: across the five files the base-to-current diff contains
**16 changed line pairs** (3 + 1 + 2 + 3 + 7), every one of them a versioned-to-versionless XSD token
substitution whose **leading whitespace is byte-identical** between the removed and the added line, and
**zero** changed lines fall outside a `springframework.org/schema/…` URL.

It was not true of an earlier revision of this change, and saying so is the point of recording it here.
Commit `1f5303b89` also re-indented the schema lines of three of these files to three tabs — style churn with
no target-stack attribution, which Rule 1 and TR1 forbid. The original leading whitespace has been restored:
**7 spaces + 2 tabs** in `applicationContext-service.xml` and `openmrs-servlet.xml`, and
**2 spaces + 2 tabs + 4 spaces** in `webModuleApplicationContext.xml`. Each of the three is now
byte-identical to its `1f5303b89^` state — `git diff 1f5303b89^ -- <file>` returns **0 lines** for all
three — so the only surviving difference from the base commit is the XSD token itself:

| Context | Grammars unpinned |
|---|---|
| `api/src/main/resources/applicationContext-service.xml` | beans, context, util (3.0) |
| `web/src/main/resources/openmrs-servlet.xml` | beans (3.0) |
| `webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml` | beans, util (3.0) |
| `test-suite/module/api/src/main/resources/moduleApplicationContext.xml` | beans 2.5, context 2.5, util 2.0 — the oldest grammars in the repository |
| `test-suite/module/omod/src/main/resources/webModuleApplicationContext.xml` | seven grammars: aop, beans, context, jee, mvc, tx, util (3.0) |

*Attribution: a Spring 7 classpath must not be driven by Spring 2.5/3.0 grammars.* The pattern was
copied from the in-repo reference implementations that were already versionless —
`api/src/test/resources/TestingApplicationContext.xml` and
`web/src/test/resources/AltAuthSchemeTestingApplicationContext.xml` — rather than from an external
convention. Bean definitions, `ListFactoryBean` and `JndiObjectFactoryBean` were left untouched.

### 4. Servlet Descriptor Completion

- `webapp/src/test/resources/override-web.xml` — the last pre-Jakarta descriptor. `xmlns` moved from
  `http://java.sun.com/xml/ns/javaee` to `https://jakarta.ee/xml/ns/jakartaee`, `web-app_3_0.xsd` to
  `web-app_6_0.xsd`, `version="3.0"` to `version="6.0"`, and the wrong-cased `XMLSchema-Instance` was
  corrected to `XMLSchema-instance`. The source pattern was the already-compliant
  `webapp/src/main/webapp/WEB-INF/web.xml`. *Attribution: the Jakarta descriptor generation supersedes
  the `javaee` namespace.*

### 5. Removed-API Documentation Repair

- `api/src/main/java/org/openmrs/api/db/hibernate/HibernateSessionFactoryBean.java` — the stale
  `@see org.springframework.orm.hibernate3.LocalSessionFactoryBean#destroy()` now reads
  `@see org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean#destroy()`. This was the only
  reference to a removed Spring package anywhere in the repository, and the class actually extends the
  relocated `org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean`. *Attribution: it named a
  package removed in the target generation.*
- `api/src/main/java/org/openmrs/api/db/hibernate/DbSession.java` — javadoc now states that
  `createCriteria(Class<T>)` returns a `jakarta.persistence.criteria.CriteriaQuery` built via
  `getCriteriaBuilder().createQuery(...)` and is **not** the removed `org.hibernate.Criteria`. The
  method name is preserved deliberately: renaming public API carries no target-stack attribution.

### 6. Rule 5 Comment-Only Annotations

Five files received comments and nothing else: `hibernate.default.properties`, `infinispan-api-local.xml`,
`OrderValidator.java`, `DrugOrderValidator.java` and `StringEnumType.java`. They are register items 1-5.
**They are the only five files this change was authorised to annotate**, which is why register items 6 and
7 are recorded in the register alone rather than at their sites — the reasons are given per item in the
defect register below.

### 7. Inspected but Unmodified

The following were inspected under Rules 2 and 3 and confirmed to declare no javax-generation coordinate,
no shading or bytecode-transformation plugin, and no pin that reintroduces the old generation:
`webapp/pom.xml`, `test/pom.xml`, `liquibase/pom.xml`, `test-suite/pom.xml`,
`test-suite/module/pom.xml`, `test-suite/module/api/pom.xml`, `test-suite/module/omod/pom.xml`,
`test-suite/performance/pom.xml`, `docker-pom.xml`, `webapp/src/main/webapp/WEB-INF/web.xml`,
`api/src/test/resources/TestingApplicationContext.xml`,
`web/src/test/resources/AltAuthSchemeTestingApplicationContext.xml`,
`api/src/main/resources/hibernate.cfg.xml`, `api/src/main/resources/infinispan-api.xml` and
`doc/JUNIT5_MIGRATION.md`.

### 8. Structural Invariants

**No module was added, removed, renamed or re-parented.** The eight-module reactor — `bom`, `tools`,
`test`, `api`, `web`, `webapp`, `liquibase`, `test-suite` — is unchanged. **No Java package moved, and no
file was relocated.** The only new path in the entire change is this document.

## Pre-Existing Defect Register

Rule 5 requires that defects found in passing be recorded in place rather than repaired, unless a repair
is needed to satisfy a validation item. **No validation item is blocked by any of the seven items below,
so none of them was fixed.**

**Where each item is recorded — stated precisely, because "all seven are commented at their sites" would
not be true:**

| Items | How they are recorded | Why |
|---|---|---|
| **1-5** | an in-place comment **at the defect's own site**, plus the register row below | all five sites are files the Agent Action Plan lists as in-scope comment-only updates (§0.2.1.5), so a site comment is both possible and attributable |
| **6** | **this register only** — no source edit | the site is 2 keys replicated across **18** locale `.properties` files. §0.6.6 row 6 of the plan classifies this item **"out of scope entirely"**, and §0.2.1.5 enumerates exactly **five** Rule-5 comment-only files, none of them a locale file. Editing 18 translation catalogues has **no** Spring/Hibernate/Jakarta/Java-21 attribution, so Rule 1 forbids it |
| **7** | **this register only** — no source edit | the site is the root `pom.xml` plugin pin, and the root POM is a **verify-only** file in this change: its planned outcome is a **byte-identical** file (§0.4.1.8 Group 8, "UPDATE (verify)"), with the skew explicitly delegated to *this document, register item 7*. Adding even a comment there would break that contract and produce an unattributable diff |

So the accurate statement is: **items 1-5 carry a site comment and a register row; items 6 and 7 carry a
register row only, each with the reason no source edit is permissible.** Nothing is repaired, nothing is
dropped, and no item is described as annotated where it is not. That is Rule 5 honoured rather than
weakened — the rule's purpose is that a discovered defect is *recorded and left alone*, and the register
is the record wherever a site comment is not an authorised edit.

| # | Site | Condition | Attributable to the target stack? |
|---|---|---|---|
| 1 | `api/src/main/resources/hibernate.default.properties` | `hibernate.connection.driver_class=com.mysql.jdbc.Driver` is the deprecated legacy driver class name; MySQL Connector/J 9.7.0 emits a deprecation notice on every test run. The modern name is `com.mysql.cj.jdbc.Driver`, which `liquibase/pom.xml` already uses | **No** — JDBC driver naming. Comment only; **the value was not changed** |
| 2 | `api/src/main/resources/infinispan-api-local.xml` | an **extra `>`** immediately after the root start-tag: `xmlns="urn:infinispan:config:15.2">>`. The file is **XML-well-formed** — the second `>` is not a syntax error but becomes an unintended **text node** child of `<infinispan>`, i.e. invalid *configuration content* rather than invalid XML. Confirmed by parsing the file: it parses cleanly and the root reports one non-whitespace text child, `">\n\t"`. The clean sibling is `infinispan-api.xml` | **No**. Comment only; **not fixed** |
| 3 | `api/src/main/java/org/openmrs/validator/OrderValidator.java` | a comment cites `Order.hbm.xml`, which **does not exist** — only `OrderFrequency`, `OrderSet`, `OrderSetAttribute` and `OrderSetMember` HBM files exist; `Order` is annotation-mapped | **No**. Comment correction only |
| 4 | `api/src/main/java/org/openmrs/validator/DrugOrderValidator.java` | the identical stale `Order.hbm.xml` citation | **No**. Comment correction only |
| 5 | `api/src/main/java/org/openmrs/api/db/hibernate/type/StringEnumType.java` | the javadoc says the class should be deleted once `Obs`, `ConceptName` and `OrderSet` move from HBM to annotations, but `ConceptName` is **already annotated**, so the stated precondition is only partly satisfied | **No**. Comment correction only. **This file was promoted from inspected-only to edited solely because of Rule 5** — a directly traceable consequence of the rule |
| 6 | 18 locale files, 2 keys each, e.g. `api/src/main/resources/messages.properties` | orphaned i18n keys for `org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler`, **a class with zero `.java` files** in the repository | **No** — **out of scope entirely**; neither deleting nor annotating 18 message catalogues is attributable. **Recorded here only, no site comment** |
| 7 | root `pom.xml` versus `bom/pom.xml` | `liquibase-maven-plugin` **4.33.0** against `liquibase-core` **4.32.0**. The plugin runs only in the `openmrs-liquibase` module's manual snapshot-generation configuration, never in the default lifecycle, so the skew cannot affect the build or the changelogs — and neither version removes the `jaxb-api` leak | **No**. **Recorded here only, no site comment** — the root POM is verify-only in this change and stays byte-identical |

### 1. Citing These Defects by Content, Not by Line

The annotation comments added for items 1, 2 and 5 shifted the lines they describe. The line references
recorded during planning were correct **at the base commit** but no longer point at the same text: the
deprecated `driver_class` moved from L6 to L7, the extra `>` from L15 to L16,
`hibernate.cache.region.factory_class` from L30 to L31, and the `StringEnumType` class declaration from
L38 to L44. These sites are therefore cited by content throughout this document. Line numbers move;
identifiers and text do not.

### 2. The Contrast That Makes Rules 1 and 5 Operable Together

**Three findings were attributable, and were therefore changed:**

1. the stale `org.springframework.orm.hibernate3` javadoc, because it names a package removed in the
   target generation;
2. the five legacy Spring XSD version pins, because 2.5 and 3.0 grammars misdescribe a Spring 7 classpath;
3. the `javax`-era `override-web.xml` root element, superseded by the Jakarta descriptor generation.

Everything else discovered in passing was **left unfixed** — annotated where it lives for the five items
whose sites this change was authorised to touch, and recorded in the register above for the two whose
sites it was not (items 6 and 7, per the table at the head of this section). That is the distinction
between Rule 1 and Rule 5 in practice: attribution decides whether a finding is repaired or merely
recorded, and it also decides *where* the record can be written.

## Behavioural-Preservation Evidence

This is a medical-records platform, so behavioural preservation is the point of the exercise. An upgrade
that changes what a service method returns, what validation rejects, or what a privilege check permits has
failed regardless of a green build.

### 1. The Frozen Advisor Chain

`api/src/main/java/org/openmrs/aop/AOPConfig.java` was **not edited** — `git diff "$BASE"..HEAD` against the base commit
reports zero changes to it. Its ordering, verified in source, is:

| Advisor | Order |
|---|---|
| `authorizationAdvisor` | 1 |
| `loggingAdvisor` | 2 |
| `requiredDataAdvisor` | 3 |
| `@EnableCaching(order = 4, proxyTargetClass = true)` | 4 |
| `@EnableTransactionManagement(order = 5, proxyTargetClass = true)` | 5 |

Advisors bind to `@Service`-annotated classes through a `StaticMethodMatcherPointcutAdvisor` whose
`matches` implementation tests `targetClass.isAnnotationPresent(Service.class)`. Module-contributed
advisors must declare either no order or an order **above 100**. **No advisor was added, removed or
reordered.**

Why this matters enough to be named: reordering these **compiles cleanly and passes a naive smoke test**
while silently changing whether privilege checks run before transaction demarcation — that is a change to
clinical-data access semantics. "Do not touch `AOPConfig.java`" was treated as a non-negotiable constraint.

### 2. Privilege Outcomes Unchanged

`AuthorizationAdvice implements MethodBeforeAdvice`, consults `Context.hasPrivilege(privilege)`, and throws
`APIAuthenticationException` carrying the message codes **`error.privilegesRequired`** and
**`error.aunthenticationRequired`**. Both the exception type and the message codes are **contract, including
the misspelling in the second code**, which must be preserved because downstream modules and translations
key on it. The file was not modified.

### 3. The Exception Contract Is Insulated by Design

Two facts have to be kept apart here, because an earlier revision of this document ran them together and
said there was "no Spring `PersistenceExceptionTranslator` or `HibernateExceptionTranslator` wired
anywhere … zero files referencing either". The first half is right; the second half is overbroad. Precisely:

- **No translation is *wired*, and none happens.** `git grep PersistenceExceptionTranslationPostProcessor`
  over the whole repository returns **zero** hits, in Java and in XML alike. That post-processor is the
  only thing that turns `@Repository` into an exception-translating proxy, and outside Spring Boot — which
  is absent from every POM — nothing registers it implicitly. So although **22** DAO classes under
  `api/src/main/java/org/openmrs/api/db` do carry `@Repository`, **no DAO is ever proxied for
  translation** and **no `DataAccessException` is ever produced** on any call path. This is the load-bearing
  fact.
- **A `PersistenceExceptionTranslator` implementation nevertheless exists in the context, by
  inheritance.** `javap` on `spring-orm-7.0.7.jar` shows
  `org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean extends
  org.springframework.orm.jpa.hibernate.HibernateExceptionTranslator`, and that class
  `implements org.springframework.dao.support.PersistenceExceptionTranslator` with a
  `translateExceptionIfPossible(RuntimeException)` method. Since
  `HibernateSessionFactoryBean extends LocalSessionFactoryBean`, the platform's own session-factory bean
  **inherits** that capability. Nothing in this repository calls it, overrides it, or registers a
  post-processor that would consume it — the capability is present and **unconsumed**. Saying "zero files
  reference either" was therefore wrong about the class hierarchy while being right about the wiring.

What actually insulates the public contract is the OpenMRS exception hierarchy, not translation. The DAO
layer declares **`throws DAOException` 590 times** across **38** files in
`api/src/main/java/org/openmrs/api/db`, and `DAOException extends APIException`, which `extends
RuntimeException`.

Because that entire hierarchy is **unchecked and OpenMRS-owned**, Jakarta and Hibernate exception-type
churn **cannot alter a single declared signature** — an unchecked exception needs no `throws` clause, so a
change of concrete provider type propagates without touching any method declaration. This is the
structural reason the requirement's *"`org.openmrs.api.*` signatures remain identical"* clause holds
**without any compatibility shim**.

It is **not**, however, a reason to call the `NonUniqueResultException` deviation in section (d)
"invisible at the service boundary" — that claim was withdrawn there. Signature stability and
concrete-type stability are different properties: the first is proven, the second is the recorded
deviation. Section (d).2 sets out exactly which of the two each piece of evidence supports.

### 4. Untouched Query Primitives

Legacy Hibernate `Criteria` supplied case-insensitivity, ordering and pagination implicitly; the migrated
code expresses them explicitly. All counts below were re-measured across
`api/src/main/java/org/openmrs/api/db/hibernate` and **none of these call sites was modified**:

| Semantic | Formerly | Now, and how often |
|---|---|---|
| Case-insensitive matching | `Restrictions.ilike` / `MatchMode` | `cb.lower(` — **57** occurrences |
| Result ordering | `Order.asc` / `Order.desc` | `cb.asc(` / `cb.desc(` — **64**; `orderBy(` — **50** |
| Pagination | `Criteria.setFirstResult` / `setMaxResults` | `setFirstResult` — **7**; `setMaxResults` — **14** |

Their correctness is a **verification obligation discharged by the test suite**, not a transformation
obligation. Legacy residue is gone: **zero executable usages** of `org.hibernate.Criteria`,
`org.hibernate.criterion` or `org.hibernate.transform` remain — no import, no type reference, no method call.

> **One textual hit, and it is documentation.** A bare
> `git grep "org.hibernate.Criteria" -- '*.java'` returns exactly **one** line, and a reader who stops there
> will think the claim above is false. The hit is `DbSession.java:171`, inside the javadoc **this change
> added**: *"Despite its legacy name, this method does not return the `org.hibernate.Criteria` that was
> …"*. Prose naming a removed type in order to warn about it is the evidence, not a violation of it — the
> same distinction recorded for gates A4/A5, where this document quotes the retired `java.sun.com`
> namespace on purpose. Scope the grep to imports and type references and the count is **zero**.

### 5. DAO Structure Preserved

Rule 4 forbids restructuring the data-access layer, and the proof is a diff rather than an inventory:
`git diff "$BASE"..HEAD` for `api/src/main/java/org/openmrs/api/db/` reports **3 files changed,
13 insertions and 1 deletion — every one of them inside a javadoc or comment block.** There is no code,
signature, package or structural change anywhere in that tree.

The 44 files in the Hibernate DAO package, the 28 DAO interfaces, the 85 `@Entity`-annotated classes, the
`DbSession` / `DbSessionFactory` seam, `JpaUtils`, `StringEnumType` and the `envers/` and `search/`
sub-packages all keep their current shape, names and locations.
`DbSession.createCriteria(Class<T>)` **kept its legacy name** because it is public API, and received a
javadoc clarification only. Rule 4 together with the schema-preservation clause is also why completing the
HBM-to-annotation migration stayed out of scope, even though `StringEnumType`'s own javadoc invites its
eventual deletion.

> **Note:** the DAO interface count above is the measured value, taken as the `*DAO.java` files under
> `api/src/main` that declare `public interface`. It is reported as measured rather than as the slightly
> different figure carried in planning; the diff-based proof in the paragraph above is what actually
> establishes preservation, and it does not depend on any count.

### 6. Spring 7 Relocations Already Absorbed

`HibernateSessionFactoryBean` extends `org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean`, and
`HibernateTransactionManager` is imported from the same relocated package in
`OpenmrsApplicationContextConfig` — both formerly `org.springframework.orm.hibernate5`. Nullability
annotations use **JSpecify** (`org.jspecify.annotations.NonNull`) rather than the removed
`org.springframework.lang.*`. These are the decisive evidence that the relocations were already applied
before this change, which is why only a stale javadoc reference remained to clean up.

### 7. Rule 2, Verified Affirmatively

The Jakarta namespace is reached through **real source and real dependency coordinates only**. Re-measured
by parsing the root POM: `<pluginManagement>` contains **exactly 23** `<plugin>` elements, and there is
**no** `maven-shade-plugin`, **no** `org.eclipse.transformer` or `transformer-maven-plugin`, **no**
`jakartaee-migration`, and **zero** `<relocation>` elements anywhere in the reactor. No repackaged javax
jar is used.

> **Note:** one nuance, recorded so a future reader does not mistake it for a violation.
> `maven-assembly-plugin` builds a `jar-with-dependencies` assembly in `liquibase/pom.xml`. That is
> **packaging only, with no `<relocation>` configured**, and it does not rewrite any namespace. It is not
> a Rule 2 violation and was left untouched.

### 8. Explicitly Excluded by the Requirement's Own Boundaries

- **Spring Boot conversion** — not attempted; the forward-looking note in `README.md` was deliberately
  left untouched.
- **Spring Data or any new persistence pattern** — not introduced.
- **XML-to-Java configuration conversion** — not attempted, even though
  `OpenmrsApplicationContextConfig` describes itself as a replacement for `applicationContext-service.xml`
  from which the project gradually migrates away, and six `@Configuration` classes already exist in `api`.
  **No bean was moved out of XML**; the edits to that context are limited to its XSD declarations.
- **Feature additions or API-surface growth** — none. No new public method, class or endpoint.

## How to Reproduce

### 1. Validation Items V1-V5

```bash
  # V1 - build exits 0 and the resolved graph is javax-free
  ./mvnw clean install -DskipTests -B          # expect exit 0, 13/13 reactor projects
  ./mvnw dependency:tree -B > deptree.txt
  grep -E "javax\.(servlet|persistence)" deptree.txt          # expect 0 matches
  grep -E "^\[INFO\][^:]*[+\\|-]- javax\." deptree.txt        # expect 0 matches
  grep -n "javax\." deptree.txt                               # expect 0 matches (broadest sweep)

  # V2 - the suite passes 100% with the skip ceiling intact
  ./mvnw test -B
  #   expect: 5,106 run / 0 failures / 0 errors / 45 skipped
  #   per module: openmrs-api 4,929/0/0/45 - openmrs-web 146/0/0/0
  #               openmrs-liquibase 24/0/0/0 - test-suite-module-api 6/0/0/0
  #               test-suite-module-omod 1/0/0/0
  #   -Pskip-default-test is required: both profiles ADD a test-phase Surefire
  #   execution without disabling default-test, so omitting it re-runs all 5,106
  ./mvnw verify -Pintegration-test -Pskip-default-test -B   # documented, NOT executed here
  ./mvnw verify -Pperformance-test -Pskip-default-test -B   # documented, NOT executed here

  # V3 - clean-database Liquibase run and mysqldump --no-data diff
  #      EXECUTED. Both sides installed by liquibase-core 4.32.0 into their OWN disposable,
  #      per-run database, each driven by a mode-600 --defaultsFile that carries the
  #      explicit URL and the credentials so nothing reaches argv - the "before" side from
  #      `git archive 3934d8086`-extracted changelog bytes, cmp-verified 38 identical /
  #      0 differing - then dumped with
  #        mysqldump --no-data --skip-comments --skip-dump-date
  #      and compared. Result: diff exit status 0, zero differing lines, both dumps
  #      byte-identical at MD5 659f521033a44a4b95e57fe0bbe105a9, 119 tables / 1,510
  #      columns / 697 indexes / 446 foreign keys / 1,028 changesets on each side, and the
  #      shared `openmrs` database asserted unchanged at 126 catalogued tables / 1,065 changesets.
  #      The full runnable script, its safety properties and the retained evidence files
  #      are in section (e).4.

  # V4 - service-output capture; the V2 run IS the capture, against the fixed
  #      DBUnit reference datasets. Expect an identical pass set, an identical
  #      skip set, and no modified assertion.

  # V5 - the namespace guard
  grep -rE "import javax\.(servlet|persistence|validation|annotation|transaction)" \
    --include=*.java .                         # expect 0 hits (already passed at baseline)
```

### 2. Additional Gates A1-A10

```bash
  # A1  removed coordinates absent from the resolved graph
  grep -E "commons-fileupload|groovy-all|commons-fileupload2" deptree.txt   # expect 0
  grep -c "liquibase-core" deptree.txt                                     # expect 6 (it must survive)

  # A2  NOTICE.md attribution is truthful: nothing attributed that is no longer shipped,
  #     while the coordinates that ARE still shipped keep their attribution.
  grep -cE 'commons-fileupload|groovy-all' NOTICE.md          # expect 0
  for c in commons-collections liquibase-core infinispan \
           jakarta.xml.bind-api jaxb-runtime type-converter; do
    printf '%-24s %s\n' "$c" "$(grep -ci "$c" NOTICE.md)"     # expect >= 1 for each
  done

  # A3  Spring contexts still load. Be exact about which run proves what: the V2 test run
  #     exercises FOUR of the five in-scope contexts, not all five.
  #       applicationContext-service.xml     the api harness, via @ContextConfiguration
  #       openmrs-servlet.xml                BaseWebContextSensitiveTest, BaseModuleWebContextSensitiveTest
  #       moduleApplicationContext.xml       BaseModuleContextSensitiveTest (test-suite-module-api)
  #       webModuleApplicationContext.xml    WebModuleActivatorTest, TestModuleControllerTest
  #       openmrs_static_content-servlet.xml NOT loaded by any test - see below
  #     Verify that last claim instead of trusting it. Nothing in the reactor names the FILE
  #     at all - it is bound by Spring's <servlet-name>-servlet.xml convention:
  git grep -n 'openmrs_static_content-servlet' -- . ':!doc/'   # expect 0 - the name is never written
  git grep -n 'openmrs_static_content'         -- . ':!doc/'   # expect 2, both in web.xml
  #     ...one declaring the servlet (class org.openmrs.web.StaticDispatcherServlet) and one
  #     mapping it to /scripts/*. web.xml deliberately omits load-on-startup, so the context
  #     is built on the FIRST /scripts/* request. The V2 run therefore cannot speak for it,
  #     and this gate needs its own two proofs - an offline bean-definition load and a
  #     request against the deployed WAR. Both are in section 4 below.
  #     For the four contexts the V2 run does load, the passing condition is unchanged:
  #     no BeanDefinitionParsingException and no XSD resolution failure anywhere in test.log.
  grep -cE 'BeanDefinitionParsingException|XmlBeanDefinitionStoreException|Failed to read schema document' test.log
  #     expect 0

  # A4  no versioned Spring grammar remains
  git ls-files '*.xml' | xargs grep -lE "spring-[a-z]+-[0-9]\.[0-9]\.xsd"  # expect 0

  # A5  no pre-Jakarta servlet descriptor remains
  git ls-files '*.xml' | xargs grep -ln "java.sun.com/xml/ns/javaee"       # expect 0

  # A4/A5 note 1: scope these two greps to TRACKED files. The bare recursive form
  #   grep -rn "java.sun.com/xml/ns/javaee" --include=*.xml .
  # returns 0 on a clean checkout but 1 on a tree that has been built, because
  # web/target/spotbugsXml.xml - generated build output, untracked and gitignored -
  # quotes the string in a report entry. That single hit is an artifact of the
  # report, not a surviving descriptor; both web.xml and override-web.xml are on
  # https://jakarta.ee/xml/ns/jakartaee. The gate is about source, so it must
  # measure source.
  #
  # A4/A5 note 2: scope them to '*.xml' - to DESCRIPTORS - and not to every tracked
  # file. Dropping the pathspec makes A5 report one hit, in THIS document, which
  # quotes the retired java.sun.com namespace on purpose when recording the
  # before/after transformation of override-web.xml. Prose describing a namespace
  # that was removed is the evidence, not a violation of it. Both gates measure
  # zero over '*.xml', which is the set that actually configures the container.

  # A6  no transformation or shading plugin was introduced (Rule 2).
  git ls-files '*pom.xml' | xargs grep -lE \
    'maven-shade-plugin|org\.eclipse\.transformer|jakartaee-migration|<relocation>'   # expect 0
  #     ...and the managed plugin set is still exactly 23. Count <plugin> ELEMENTS, not
  #     <artifactId> lines: a naive grep also counts the artifactIds of plugin-scoped
  #     <dependencies>, which on this pom.xml inflates 23 to 27.
  python3 -c "import xml.etree.ElementTree as E; n={'m':'http://maven.apache.org/POM/4.0.0'}; \
print(len(E.parse('pom.xml').getroot().find('m:build/m:pluginManagement/m:plugins',n) \
.findall('m:plugin',n)))"                                                             # expect 23

  # A7  frozen artifacts untouched. Compare COMMITTED state against the base commit -
  #     a worktree-only `git diff` / `git status` reports nothing at a clean HEAD and
  #     therefore cannot verify a committed change at all.
  #     The pathspec must name EVERY frozen artifact, including the seed database dump
  #     initial_test_db.sql - which lives at the REPOSITORY ROOT, not under api/. A frozen
  #     artifact absent from the pathspec is simply not gated.
  BASE=3934d8086c684269e935562f25e806be91947115
  FROZEN=(
    'api/src/main/resources/liquibase-*.xml'
    'api/src/main/resources/org/openmrs/liquibase/**'
    'api/src/main/resources/**/*.hbm.xml'
    api/src/main/resources/hibernate.cfg.xml
    api/src/main/java/org/openmrs/aop/AOPConfig.java
    api/src/main/java/org/openmrs/aop/AuthorizationAdvice.java
    webapp/src/main/webapp/WEB-INF/web.xml
    initial_test_db.sql
  )
  git diff --name-status "$BASE"..HEAD -- "${FROZEN[@]}"           # expect NO output
  git status --porcelain -- "${FROZEN[@]}"                         # expect NO output (worktree too)
  # Prove the gate is not vacuous - a pathspec that matches nothing also prints nothing.
  # Count per pattern, so a single silently-unmatched pattern cannot hide behind the others:
  for p in "${FROZEN[@]}"; do
    printf '%-52s %s\n' "$p" "$(git ls-files -- "$p" | wc -l)"     # expect every count >= 1
  done

  # A8  build wall clock shows no change of ORDER. Read Maven's own "Total time" line
  #     rather than an external stopwatch, and treat it as a sanity check, not a
  #     benchmark: neither the pre- nor the post-change run controlled for CPU
  #     contention or repository warmth, so a minutes-vs-minutes comparison is all
  #     that is supportable. Both logs must be RETAINED for the grep to have anything to
  #     read: a timing quoted from a log the run did not keep is a figure no reader can
  #     check.
  ./mvnw clean install -DskipTests -B > install.log 2>&1; echo "install exit=$?"
  ./mvnw test -B                     > test.log    2>&1; echo "test exit=$?"
  grep -E '^\[INFO\] Total time' install.log test.log
  grep -cE 'SUCCESS \[' install.log            # expect 13 - one per reactor project
  #     Maven prints no reactor-WIDE test total, so sum the five per-module `Results:` blocks.
  #     Do not tail the last one: `grep ... | tail -1` returns the final module's own total,
  #     which is openmrs-test-suite-module-omod's single test - it looks like a passing check
  #     while saying nothing about 5,106.
  grep -E '^\[INFO\] Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$' test.log \
    | awk -F'[:,]' '{r+=$2;f+=$4;e+=$6;s+=$8;n++} \
        END{printf "blocks=%d run=%d failures=%d errors=%d skipped=%d\n",n,r,f,e,s}'
  #     expect exactly: blocks=5 run=5106 failures=0 errors=0 skipped=45

  # A9  formatting conforms. The -D flag is LOAD-BEARING: the root pom.xml sets
  #     <spotless.check.skip>true</spotless.check.skip> by default and only the
  #     ci-checks profile flips it, so a bare `./mvnw spotless:check` prints
  #     "Spotless check skipped" for every module and STILL EXITS 0 - a vacuous pass.
  ./mvnw spotless:check -B -Dspotless.check.skip=false      # expect BUILD SUCCESS, 0 violations
  #     Measured, so that "it passed" is checkable: 12 of the 13 reactor projects genuinely
  #     execute the goal. Only `openmrs-bom` prints "Spotless check skipped", and it does so
  #     for its own reason - bom/pom.xml sets <skip>true</skip> in its plugin configuration,
  #     which the -D flag does not override. The six Java-carrying modules report
  #     1185 + 68 + 8 + 9 + 2 + 1 = 1,273 files "keeping ... clean - 0 needs changes":
  #     the same 1,273 .java files the V5 namespace guard scans.

  # A10 this document exists AND is complete. `test -f` is a PRESENCE check, not a
  #     completeness check - it passes on a zero-byte file - so assert the structure and
  #     the mandated sections instead.
  DOC=doc/JAKARTA_MIGRATION_BASELINE.md
  if [ ! -f "$DOC" ]; then echo "FAIL: $DOC is missing" >&2; exit 1; fi
  if [ "$(grep -c '^# ' "$DOC")" -ne 1 ]; then echo 'FAIL: not exactly one H1' >&2; exit 1; fi
  if [ -n "$(tail -c 1 "$DOC")" ]; then echo 'FAIL: no terminating newline' >&2; exit 1; fi
  if [ $(( $(grep -c '^```' "$DOC") % 2 )) -ne 0 ]; then
    echo 'FAIL: unbalanced fenced code block' >&2; exit 1
  fi
  if grep -qE ' +$' "$DOC"; then echo 'FAIL: trailing whitespace' >&2; exit 1; fi
  for h in 'Section (a):' 'Section (b):' 'Section (c):' 'Section (d):' 'Section (e):' \
           'Section (f):' 'Change Inventory' 'Pre-Existing Defect Register' \
           'Behavioural-Preservation Evidence' 'How to Reproduce' 'Definition of Done'; do
    if ! grep -qF "## $h" "$DOC"; then echo "FAIL: missing section '$h'" >&2; exit 1; fi
  done
  echo 'PASS: A10 - document present, structurally sound, all mandated sections found'
```

**A10's assertions were shown to bite, not merely to pass.** Six mutations, applied one at a time to a
*copy* of this document, were each rejected with the message that names the specific defect: a second `# `
H1 → `FAIL: not exactly one H1`; the terminating newline stripped → `FAIL: no terminating newline`; one
fence deleted → `FAIL: unbalanced fenced code block`; a line given trailing spaces → `FAIL: trailing
whitespace`; `## Definition of Done` deleted → `FAIL: missing section 'Definition of Done'`; the file
removed → `FAIL: … is missing`. The unmutated copy passed. That is the same standard the schema validation
in section 4 is held to — a check that cannot fail is not evidence.

**Per-gate measured results.** An earlier revision summarised this as "all of A1 through A10 were run and
all passed", which is an attestation a reader cannot check — and which had in fact survived one revision in
which two of the ten gates were vacuous. The block above was therefore extracted from *this document* and
executed verbatim, and every line it printed is reproduced here:

| Gate | Command output, as printed | Expected | Verdict |
|---|---|---|---|
| A1 | removed-coordinate hits **0**; `liquibase-core` nodes **6** | 0; 6 | PASS |
| A2 | `commons-fileupload\|groovy-all` in `NOTICE.md` **0**; `commons-collections` 1, `liquibase-core` 1, `infinispan` 1, `jakarta.xml.bind-api` 1, `jaxb-runtime` 1, `type-converter` 1 | 0; each ≥ 1 | PASS |
| A3 | `openmrs_static_content-servlet` **0** hits; `openmrs_static_content` **2** hits (`web.xml:274`, `web.xml:323`); parse/schema failures in `test.log` **0** | 0; 2; 0 | PASS |
| A4 | versioned Spring grammars over tracked `*.xml` — no output | 0 | PASS |
| A5 | `java.sun.com/xml/ns/javaee` over tracked `*.xml` — no output | 0 | PASS |
| A6 | shade/transformer/relocate — no output; managed `<plugin>` elements **23** | 0; 23 | PASS |
| A7 | committed diff vs base — no output; worktree status — no output; per-pattern counts **6, 32, 20, 1, 1, 1, 1, 1** | no output ×2; each ≥ 1 | PASS |
| A8 | `SUCCESS [` count **13**; `blocks=5 run=5106 failures=0 errors=0 skipped=45`; `Total time` **01:20 min** / **10:42 min** on run 1 and **01:21 min** / **10:47 min** on the run-2 re-execution — this gate rewrites both logs, so the timings are a sanity check on *order*, not a value to reproduce | 13; 5,106/0/0/45 | PASS |
| A9 | `BUILD SUCCESS`, **0 violations**, no file reformatted | 0 violations | PASS |
| A10 | `PASS: A10 - document present, structurally sound, all mandated sections found` | that line, exit 0 | PASS |

Four of those rows carry a caveat that belongs with the number rather than in a footnote. **A4 and A5**
measure *tracked* `*.xml` only, for the two reasons given in the block above — generated
`web/target/spotbugsXml.xml` quotes the retired namespace in a report entry, and this document quotes it on
purpose when recording the `override-web.xml` transformation. **A7**'s per-pattern counts are the
non-vacuity proof: a pathspec matching nothing also prints nothing, so each of the eight frozen patterns is
counted separately, and the count of **1** against `initial_test_db.sql` is the one an earlier revision of
the gate could not have produced, because the pattern was absent. **A8** is satisfied only in the sense the
evidence supports — both phases minutes-scale and both exiting 0, with **no** speed comparison drawn against
the planning figures, for the reason set out in section (a).1; both timings are quoted from the `Total time`
line of a **retained** log, which is why the gate now redirects to `install.log` and `test.log` instead of
discarding the output. **A9**'s pass is non-vacuous only because of the explicit
`-Dspotless.check.skip=false`, and 12 of the 13 projects genuinely execute the goal.

### 3. Reading a Negative Grep Correctly

Most gates above are **negative** checks: success means *no output*. `grep` signals that with **exit
status 1**, not 0, so a naive `&&` chain or a `set -e` script treats a passing gate as a failure. Wrap
each negative check so the intent is explicit:

```bash
  if grep -Eq '^\[INFO\][^:]*[+\\|-]- javax\.' deptree.txt; then
    echo 'FAIL: unexpected javax dependency-tree node' >&2
    exit 1
  fi
  echo 'PASS: no javax dependency-tree node'
```

The same shape applies to V5, A1, A4 and A5. Every "expect 0 matches" comment in this document means
*grep printed nothing and exited 1*, which is the passing outcome.

### 4. Gate A3 in Full: What the Test Run Proves, and What It Cannot

An earlier revision of gate A3 asserted that the contexts are "exercised by every context-sensitive test
in the V2 run". For four of the five in-scope contexts that is true and citable. For the fifth it is
false, and the gate is now split accordingly rather than left as a claim a reader cannot check.

| In-scope context | Loaded by | Citation |
|---|---|---|
| `api/src/main/resources/applicationContext-service.xml` | the `api` harness, for every context-sensitive test | `@ContextConfiguration(locations = { "classpath:applicationContext-service.xml", … })` at `BaseContextSensitiveNonTransactionalTest:115` and `BaseModuleContextSensitiveTest:25` |
| `web/src/main/resources/openmrs-servlet.xml` | the `web` harnesses | `classpath*:openmrs-servlet.xml` at `BaseWebContextSensitiveTest:24` and `BaseModuleWebContextSensitiveTest:26` |
| `test-suite/module/api/src/main/resources/moduleApplicationContext.xml` | the `api` harness by wildcard, when the module is on the classpath | `classpath*:moduleApplicationContext.xml` at `BaseContextSensitiveNonTransactionalTest:115`, `BaseModuleContextSensitiveTest:25` |
| `test-suite/module/omod/src/main/resources/webModuleApplicationContext.xml` | `WebModuleActivatorTest` and the module web harness | `classpath*:webModuleApplicationContext.xml` at `WebModuleActivatorTest:47`, `BaseModuleWebContextSensitiveTest:26` |
| `webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml` | **no test at all** | see below |

The fifth context is not referenced by name **anywhere** in the reactor —
`git grep -n 'openmrs_static_content-servlet' -- . ':!doc/'` returns **0 hits**, and the broader
`git grep -n 'openmrs_static_content'` returns exactly **2**, both in
`webapp/src/main/webapp/WEB-INF/web.xml`: the servlet declaration at line 274
(`<servlet-class>org.openmrs.web.StaticDispatcherServlet</servlet-class>`, itself a subclass of Spring's
`DispatcherServlet`) and its `/scripts/*` mapping at line 323. The file is bound to that servlet purely by
Spring's `<servlet-name>-servlet.xml` convention, and `web.xml` deliberately omits `load-on-startup` —
"Don't use load-on-startup in case initial setup wizard is needed" — so the context is built on the
**first `/scripts/*` request**, in a servlet container, which is precisely what a `mvn test` run never does.

It therefore gets two proofs of its own.

**Proof 1 — offline bean-definition load and grammar validation.** One self-contained program, run with the
Java 21 single-file source launcher against the `web` module's own classpath, so both the XSDs and the
Spring generation are the ones the build resolves. Part 1 validates each context with a *validating* parser
whose `EntityResolver` is Spring's own `PluggableSchemaResolver`, which maps every
`http://www.springframework.org/schema/…` URL to the copy inside the jars — no network, and the grammar
exercised is the shipped one. Part 2 reads the orphan context's bean definitions.

```java
  import java.io.File;
  import javax.xml.parsers.DocumentBuilder;
  import javax.xml.parsers.DocumentBuilderFactory;

  import org.springframework.beans.factory.support.DefaultListableBeanFactory;
  import org.springframework.beans.factory.xml.PluggableSchemaResolver;
  import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
  import org.springframework.core.io.FileSystemResource;
  import org.xml.sax.ErrorHandler;
  import org.xml.sax.SAXParseException;

  public class A3ContextProof {

  	public static void main(String[] args) throws Exception {
  		int invalid = 0;
  		for (String path : args) {
  			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
  			f.setNamespaceAware(true);
  			f.setValidating(true);
  			f.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
  				"http://www.w3.org/2001/XMLSchema");
  			DocumentBuilder b = f.newDocumentBuilder();
  			b.setEntityResolver(new PluggableSchemaResolver(A3ContextProof.class.getClassLoader()));
  			final int[] errs = new int[1];
  			b.setErrorHandler(new ErrorHandler() {
  				public void warning(SAXParseException e) { System.out.println("  WARN  " + e.getMessage()); }
  				public void error(SAXParseException e) { errs[0]++; System.out.println("  ERROR " + e.getMessage()); }
  				public void fatalError(SAXParseException e) throws SAXParseException { errs[0]++; throw e; }
  			});
  			b.parse(new File(path));
  			System.out.println((errs[0] == 0 ? "VALID   " : "INVALID ") + path);
  			invalid += errs[0] == 0 ? 0 : 1;
  		}

  		String orphan = "webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml";
  		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
  		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
  		reader.setValidating(true);
  		int count = reader.loadBeanDefinitions(new FileSystemResource(orphan));
  		System.out.println("LOADED " + count + " bean definitions from " + orphan);
  		for (String name : factory.getBeanDefinitionNames()) {
  			System.out.println("  bean " + name + " -> " + factory.getBeanDefinition(name).getBeanClassName());
  		}

  		System.out.println("contexts=" + args.length + " invalid=" + invalid + " definitions=" + count);
  		if (invalid != 0 || count == 0) {
  			System.exit(1);
  		}
  	}
  }
```

Resolve the classpath once, then run it over all five contexts:

```bash
  ./mvnw -B -q -o -pl web dependency:build-classpath \
    -Dmdep.outputFile="$PWD/web_cp.txt" -Dmdep.includeScope=test
  java -cp "$(cat web_cp.txt)" A3ContextProof.java \
    api/src/main/resources/applicationContext-service.xml \
    web/src/main/resources/openmrs-servlet.xml \
    webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml \
    test-suite/module/api/src/main/resources/moduleApplicationContext.xml \
    test-suite/module/omod/src/main/resources/webModuleApplicationContext.xml
```

Measured output, verbatim, exit status **0**:

```text
  VALID   api/src/main/resources/applicationContext-service.xml
  VALID   web/src/main/resources/openmrs-servlet.xml
  VALID   webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml
  VALID   test-suite/module/api/src/main/resources/moduleApplicationContext.xml
  VALID   test-suite/module/omod/src/main/resources/webModuleApplicationContext.xml
  LOADED 6 bean definitions from webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml
    bean viewResolver -> org.springframework.web.servlet.view.InternalResourceViewResolver
    bean handlerMapping -> org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
    bean staticResourceDispatcher -> org.openmrs.web.StaticResourceDispatcher
    bean urlRewrites -> org.springframework.beans.factory.config.MapFactoryBean
    bean staticContentController -> org.openmrs.web.controller.PseudoStaticContentController
    bean jstlContentController -> org.openmrs.web.controller.PseudoStaticContentController
  contexts=5 invalid=0 definitions=6
```

**The validation is not vacuous, and that was demonstrated rather than assumed.** A deliberately invalid
context — a `<beans>` document containing `<notAnElement id="x"/>` under the same versionless
`spring-beans.xsd` reference — is rejected:

```text
  ERROR cvc-complex-type.2.4.a: Invalid content was found starting with element
  '{"http://www.springframework.org/schema/beans":notAnElement}'. One of
  '{…:description, …:import, …:alias, …:bean, WC[##other:…], …:beans}' is expected.
  INVALID blitzy_adhoc_tmp/negative-control.xml
  contexts=1 invalid=1 definitions=6
```

It prints `INVALID`, reports `invalid=1` and exits **1**. A validator that accepted everything — the usual
failure mode of a schema check that silently could not fetch its grammar — would have passed it.

**Proof 2 — the context initialized and served, inside a real servlet container.** Proof 1 establishes that
the grammar resolves and that the six definitions parse. It cannot establish that the servlet starts, that
the handler chain runs, or that the `urlRewrites` map is ever consulted, because none of that happens outside
a container. So the WAR produced by the A8 `install` was deployed to Tomcat 11.0.21 on JDK 21, and the two
URL families this context governs were requested:

```bash
  # deploy the WAR the A8 install produced, onto Tomcat 11 / JDK 21, port 8080
  cp webapp/target/openmrs.war /opt/tomcat11/webapps/openmrs.war
  /opt/tomcat11/bin/startup.sh

  # the canonical URL, then the legacy URL that util:map rewrites onto it
  curl -s -o /tmp/canon.js  -w '%{http_code} %{size_download} %{content_type}\n' \
    http://localhost:8080/openmrs/scripts/jquery/jquery.min.js
  curl -s -o /tmp/legacy.js -w '%{http_code} %{size_download} %{content_type}\n' \
    http://localhost:8080/openmrs/scripts/jquery/jquery-1.3.2.min.js
  cmp /tmp/canon.js /tmp/legacy.js && echo IDENTICAL

  # the file exists ONLY under WEB-INF, and the container must refuse that path directly
  find /opt/tomcat11/webapps/openmrs -maxdepth 2 -name scripts -not -path '*/WEB-INF/*'   # expect no output
  curl -s -o /dev/null -w '%{http_code}\n' \
    http://localhost:8080/openmrs/WEB-INF/view/scripts/jquery/jquery.min.js               # expect 404
  # a missing script must come back 404, not 500
  curl -s -o /dev/null -w '%{http_code}\n' \
    http://localhost:8080/openmrs/scripts/does-not-exist-1234.js                          # expect 404
```

Measured against the WAR from that `install` — 141,023,727 bytes, copied at 16:02:20, exploded at 16:02:22,
`Server startup in [14842] milliseconds`. Every byte count below was read from the browser's own network
activity and independently agrees with the `content-length` header, `PerformanceNavigationTiming`'s
`encodedBodySize` and `decodedBodySize`, the in-DOM text length, the saved response body on disk, and
Tomcat's `localhost_access_log`:

| # | Request | Status | Content-Type | Bytes | Redirects |
|---|---|---|---|---|---|
| 1 | `/openmrs/scripts/jquery/jquery.min.js` (canonical) | **200** | `text/javascript;charset=UTF-8` | **93,868** | 0 |
| 2 | `/openmrs/scripts/jquery/jquery-1.3.2.min.js` (legacy, rewritten) | **200** | `text/javascript;charset=UTF-8` | **93,868** | 0 |
| 3 | `/openmrs/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js` | **200** | `text/javascript;charset=UTF-8` | **207,478** | 0 |
| 4 | `/openmrs/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css` | **200** | `text/css;charset=UTF-8` | **31,854** | 0 |
| 5 | `/openmrs/scripts/this-path-does-not-exist-9876.js` | **404** | `text/html;charset=utf-8` | 829 | 0 |
| 6 | `/openmrs/` (application root) | **404** | `text/html;charset=utf-8` | 739 | 0 |

Requests 1 and 2 returned **byte-identical** bodies: `cmp` reports 0 differing bytes, both hash to SHA-256
`88171413fc76dda23ab32baa17b11e4fff89141c633ece737852445f1ba6c1bd`, and both responses carry the identical
`ETag: W/"93868-1785472180000"`, so the server served the same underlying entity. It did so **without a
redirect**, established four independent ways: `redirectCount`, `redirectStart` and `redirectEnd` are all 0;
`location.href` still ends in `jquery-1.3.2.min.js`, so the address bar never changed; there is no `Location`
header; and the access log records exactly one server-side request for the legacy URL with no follow-on
request to the canonical one. The session histogram is **6 × 200, 3 × 404, zero 3xx, zero 5xx** — the third
404 being Chrome's own unsolicited `/favicon.ico` probe against the Tomcat ROOT context.

**Four independent lines of evidence make this a proof of initialization rather than a smoke test:**

- **The 200s are not attributable to the default servlet.** Every one of those files exists *only* under
  `WEB-INF/view/`; `find` confirms there is no `scripts/` directory anywhere outside `WEB-INF` in the exploded
  application, and the container refuses the underlying path directly —
  `/openmrs/WEB-INF/view/scripts/jquery/jquery.min.js` returns **404**. A 200 on `/scripts/…` is therefore
  only reachable through `viewResolver`'s `/WEB-INF/view` prefix, that is, through this context.
- **All three `urlRewrites` entries demonstrably fired, provable from content rather than from byte counts.**
  The URL asking for jQuery **1.3.2** returned a body whose banner reads
  `/*! jQuery v1.7.1 jquery.com | jquery.org/license */` and whose code declares `jquery:"1.7.1"`; the URL
  asking for jQuery UI **1.7.2** returned the `jQuery UI 1.8.2` banner with `c.extend(c.ui,{version:"1.8.2"…})`;
  and the `redmond` CSS URL returned the file carrying the Redmond ThemeRoller signature
  (`bgColorHeader=5c9ccc`, `bgTextureHeader=12_gloss_wave.png`). Those substitutions can only come from the
  `<util:map id="urlRewrites">` bean — which is resolved through the `util` namespace whose XSD this change
  made **versionless**. That is direct runtime proof the versionless grammar resolves on the shipped classpath.
- **Both 404s are informative, not merely non-failing.** Request 5's body reads `The requested resource
  [/openmrs/WEB-INF/view/scripts/this-path-does-not-exist-9876.js] is not available` — the `/WEB-INF/view`
  segment is absent from the request URL and could only have been prepended by an initialized
  `InternalResourceViewResolver`, so the whole handler chain ran and only the final file lookup failed.
  Request 6's body reads `No endpoint GET /openmrs/.`, which is Spring `DispatcherServlet`'s own no-handler
  wording rather than the container's — two *different* live servlets produced the two 404s. A root 404 is the
  correct result for a core WAR whose user interface ships in downstream `.omod` modules.
- **The container logged the initialization explicitly.** `/opt/tomcat11/logs/localhost.<date>.log` records
  `INFO [http-nio-8080-exec-2] org.apache.catalina.core.ApplicationContext.log Initializing Spring
  StaticDispatcherServlet 'openmrs_static_content'` at 16:03:05.916 — the lazy initialization firing on the
  first `/scripts/*` request after the 16:02:22 deployment, with no error after it.

Searching all three Tomcat log files and all six response bodies for
`BeanDefinitionParsingException`, `XmlBeanDefinitionStoreException`, `SAXParseException` and
`Failed to read schema document` returns **0 hits**, as does a scan for stack-trace markers; the `<pre>`
element count on both error pages is **0**, which is exactly where Tomcat would render a trace. The browser
console produced exactly one distinct message for the whole session — an `error`-level
`Failed to load resource: the server responded with a status of 404 ()`, attributable to the two intentional
404 navigations and the favicon probe — with zero warnings, zero JavaScript errors, and no console output at
all from requests 1 through 4. The shared `openmrs` database was **unchanged** by the deployment: 126 tables
and 1,065 applied changesets both before and after, so this proof neither depended on nor altered schema
state.

Two disclosures, so that the evidence is not read as stronger than it is. First, a screen recording of the
same six-URL flow exists as motion evidence only: in that second pass, requests 1 through 4 were satisfied
from Chrome's HTTP cache — those responses carry `ETag` and `Last-Modified` — so only the two 404s reached
the server. Every status, content type and byte count quoted above comes from the first pass, whose six
server round-trips are individually present in the access log. Second, the day's access log also holds five
`500` responses, all on `GET /openmrs/moduleResources/nonexistent` and all timestamped roughly ninety minutes
*before* this deployment, so they belong to an earlier deployment in an earlier session and to a URL that is
not under test here. They are outside this document's scope, are not caused by anything this change touches,
and are recorded only so that the log audit is not presented as cleaner than it is.

## Definition of Done

Each box below cites the value that was measured, not merely the fact that a check was run. Where an earlier
revision of this list asserted an outcome in the abstract, the number that settles it now appears beside it.

- [x] `./mvnw clean install -DskipTests -B` exits **0** with all **13** reactor projects reporting
      `SUCCESS` — `grep -cE 'SUCCESS \[' install.log` = **13**, twice, on two independent runs of the same
      tree (`Total time` **01:20 min** then **01:21 min**). The count of 13 is the reproducible part; the
      wall clock is a per-run sample, and gate A8 overwrites the log it is read from.
- [x] `./mvnw test -B` reports exactly **5,106 run / 0 failures / 0 errors / 45 skipped**, arrived at by
      summing the five per-module `Results:` blocks — 4,929 + 146 + 24 + 6 + 1, with all 45 skips in
      `openmrs-api` — rather than by transcription. No assertion was modified, no test deleted and no new
      `@Disabled` added, and that is measured rather than asserted: **0** test-source files differ from the
      base commit and **0** `@Disabled` lines were added to any `.java` file. The 45 is a ceiling, and it
      held.
- [x] `./mvnw dependency:tree -B` shows **zero `javax.*` dependency-tree nodes** — not merely zero
      `servlet` and `persistence` nodes — across all **1,620** lines of output.
- [x] `grep -rE "import javax\.(servlet|persistence|validation|annotation|transaction)"` returns zero hits,
      confirmed three independent ways on a *built* tree: the recursive `--include=*.java` form, a
      tracked-files `git grep`, and a recursive scan over every file type — **0, 0 and 0**. The **94**
      surviving `javax.*` imports are all JDK-shipped and out of scope by definition (`javax.xml` 73,
      `javax.swing` 8, `javax.imageio` 6, `javax.crypto` 5, `javax.sql` 2).
- [x] The three removed coordinates are absent from the graph and from `NOTICE.md` — **0** hits in each —
      and no other attribution was disturbed: `commons-collections`, `liquibase-core`, `infinispan`,
      `jakarta.xml.bind-api`, `jaxb-runtime` and `type-converter` each still resolve to **≥ 1** line.
- [x] No file under the frozen sets appears in **either** the committed-range `git diff --name-status
      "$BASE"..HEAD` **or** the worktree `git status --porcelain` over the frozen pathspec — both return no
      output. The pathspec names **eight** patterns, `initial_test_db.sql` among them, and each is
      separately proven to match at least one tracked file (**6, 32, 20, 1, 1, 1, 1, 1**), so no pattern
      passes by matching nothing.
- [x] `./mvnw spotless:check -B -Dspotless.check.skip=false` reports **0 violations** with no file
      reformatted. The `-D` flag is load-bearing — without it the goal is skipped and still exits 0 — and
      **12** of the 13 projects genuinely execute it, covering **1,273** `.java` files.
- [x] This document exists **and is structurally complete**, which is a stronger claim than presence and is
      checked as such: gate A10 asserts a single `# ` H1, a terminating newline, balanced fences, no
      trailing whitespace and every mandated section heading, and those assertions were shown to reject six
      deliberate mutations of a copy.
- [x] Every UPDATE is traceable to a **named attribution** — of the two distinct kinds set out at the head
      of the Change Inventory: the target generation for the migration edits, and **Rule 5** for the five
      comment-only annotations, which by definition are *not* target-stack-attributable. Every one of the
      seven pre-existing defect register items is **left unfixed and recorded** — items 1-5 with an in-place
      comment at their own site, items 6 and 7 in the register only, because the 18 locale catalogues and
      the byte-identical verify-only root `pom.xml` are not files this change may annotate. See the
      "where each item is recorded" table in the Pre-Existing Defect Register.
- [x] The one in-scope Spring context that **no test loads** —
      `webapp/src/main/webapp/WEB-INF/openmrs_static_content-servlet.xml`, referenced by name **0** times
      in the reactor — is proven to work by its own two proofs rather than covered by a claim about the
      test run: an offline `XmlBeanDefinitionReader` load of its **6** bean definitions under the
      versionless grammars, and a deployed-WAR request pass on Tomcat 11 in which the canonical and the
      internally rewritten URL both return **HTTP 200** with byte-identical **93,868**-byte bodies and no
      redirect. Section 4 of "How to Reproduce" carries both, with their negative controls.
- [x] The clean-database Liquibase run and `mysqldump --no-data` schema diff — **executed**, not merely
      documented: two disposable, per-run databases installed by `liquibase-core` 4.32.0 from
      base-commit and current changelog bytes, both dumped and compared. **`diff` exit status 0, zero
      differing lines, both dumps byte-identical at MD5 `659f521033a44a4b95e57fe0bbe105a9`
      (3,389 lines / 175,156 bytes each), 119 tables / 1,510 columns / 697 indexes / 446 foreign keys /
      1,028 changesets on each side, and the shared `openmrs` database asserted unchanged at
      126 catalogued tables / 1,065 changesets.** The comparison is also proven *symmetric* before it is
      believed: the two changelog file **sets** are compared sorted before any byte comparison
      (`38 files compared, 0 differing`, empty file-set diff), the current side is staged through an
      identically shaped root that is itself `cmp`-verified (`38 files copied from the working tree,
      0 differing`), and the two Liquibase engine logs are diffed after timestamp normalisation to
      **0 lines** — **1,169** lines and **1,028** `Running Changeset:` entries on each side, with the same
      **116** benign primary-key naming warnings and **zero** `ServiceConfigurationError`. That last check
      is what caught a real asymmetry in an earlier revision of the script, where the two sides ran with
      different classpath roots and their logs diverged by 135 lines. The runnable script — no credential in argv, fail-closed creation
      under an `flock`, an idempotent `EXIT`-only cleanup that owns only what it created, and `INT`/`TERM`
      handlers that exit 130/143 — and the retained evidence files are in section (e).4.

## Further Reading

- [`doc/JUNIT5_MIGRATION.md`](JUNIT5_MIGRATION.md) — the companion migration guide, and the document
  pattern this file follows
- [`NOTICE.md`](../NOTICE.md) — third-party attribution, kept in step with every dependency removal
- [`bom/pom.xml`](../bom/pom.xml) — the single version and exclusion control point (TR3)
- [Jakarta EE Specifications](https://jakarta.ee/specifications/)
- [Hibernate ORM 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/main/migration-guide.adoc)
- [Spring Framework 6 Upgrade Notes](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x)
- [OpenMRS Wiki](https://wiki.openmrs.org/)
