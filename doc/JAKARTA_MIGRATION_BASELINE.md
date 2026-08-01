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

It is the **direct parent of `9124e3dab`**, the commit that created this document — the seventh of the nine
commits on top of the base commit. No figure in this document was ever derived from a failed lookup; the
erroneous sentence was a wrong claim about Git history, not a corrupted measurement.

**Which baseline supports which measurement.** The two candidate baselines are not interchangeable, and the
difference between them is exactly one file:

| Baseline | What it is | What it supports |
|---|---|---|
| `3934d8086c684269e935562f25e806be91947115` | the Agent Action Plan's base commit ("chore: add TechDocs scaffold (#1)", 2026-05-15), parent of the first change in this work | **every "pre-change" column and every `"$BASE"` in every command block in this document**, including the section (e) `git archive` extraction of the pre-change changelog bytes |
| `2cbf9d7f8762451bdf253455c6e988eef732b843` | the tree with **all** code and configuration changes already applied and this evidence document not yet written | the review pass that nominated it; **no measurement in this document is taken against it** |

That second row carries a consequence worth stating, because it makes the choice of baseline immaterial for
every code claim: `git diff --name-status 2cbf9d7f8..HEAD` reports exactly **one** path,
`A doc/JAKARTA_MIGRATION_BASELINE.md`, while `git diff --name-status 3934d8086..2cbf9d7f8` reports the
other **18** — every POM, descriptor, Java and `NOTICE.md` edit in this work. So **all 18 code and
configuration changes are already present at `2cbf9d7f8`**, and any build, test, dependency-tree or schema
measurement taken there is identical to one taken at HEAD. A review comparing against `2cbf9d7f8` and this
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
than taken on trust. An earlier revision of this section reported an `install` of 1:21 and a `test` of
10:07 without saying which run produced them; those figures are replaced here by a single, fully attributed
set.

**The capture.** Commit `d27df94de010d8e3e43bac20e31ab3f7213495ea` (the tip of this work) with this
document's own in-flight edits in the working tree — no source, POM or test file differs from that commit.
Run on **2026-08-01** on **JDK 21.0.11** (`OpenJDK Runtime Environment build 21.0.11+10-1-25.10.2-Ubuntu`)
with **Apache Maven 3.9.9** via the repository's own `./mvnw`, against a **fully warmed** local repository,
with `CI` **unset** so Spotless runs in `apply` mode exactly as a developer's build does. Four commands,
run in this order, each captured to its own log:

| # | Command | Result | Maven `Total time` | Exit |
|---|---|---|---|---|
| 1 | `./mvnw -B clean install -DskipTests` | `BUILD SUCCESS`, **13 of 13** reactor projects `SUCCESS` | **01:21 min** | **0** |
| 2 | `./mvnw -B test` | `BUILD SUCCESS`, **5,106 run / 0 failures / 0 errors / 45 skipped**, 13 of 13 `SUCCESS` | **09:53 min** | **0** |
| 3 | `./mvnw -B dependency:tree` | `BUILD SUCCESS`, **1,620 lines**, **zero** `javax.` occurrences of any kind | **2.405 s** | **0** |
| 4 | `./mvnw -B spotless:check -Dspotless.check.skip=false` | `BUILD SUCCESS`, **0 violations** | **2.024 s** | **0** |

Command 2 is the run behind the **post-change** columns of the per-module test table at the head of this
section. Its wall clock is dominated by two modules: `openmrs-api` **07:59 min** and `openmrs-web`
**01:24 min**, with the remaining eleven reactor projects totalling under 30 seconds between them.

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
materialised with `git archive 3934d8086`, and each of the **38** extracted files was verified against its
working-tree counterpart with `cmp` — **38 identical, 0 differing**. Both sides were then installed into
their own **disposable, clone-scoped** database by the **same** Liquibase engine — `liquibase-core`
**4.32.0** (`version 4.32.0 #8159 built at 2025-05-19`), resolved from the `api` module's own classpath so
that engine parity between the two sides is guaranteed by construction — driving
`liquibase-schema-only.xml` with the platform's own bookkeeping table names
(`--databaseChangelogTableName=liquibasechangelog`,
`--databaseChangelogLockTableName=liquibasechangeloglock`, matching
`DatabaseUpdater.setDatabaseChangeLogTableName`). Each database was then dumped with
`mysqldump --no-data --skip-comments --skip-dump-date`.

The measurements below are the ones this run produced. Every one is reproducible with the script in
(e).4, and every raw artifact it names was retained by the run that produced them.

| Measurement | Before side (base-commit changelog bytes) | After side (current changelog bytes) |
|---|---|---|
| Changesets executed (Liquibase log) | **1,028** | **1,028** |
| Rows in `liquibasechangelog` | **1,028** | **1,028** |
| Base tables | **119** | **119** |
| Columns | **1,510** | **1,510** |
| Distinct indexes | **697** | **697** |
| Foreign keys | **446** | **446** |
| `CREATE TABLE` statements in the dump | **119** | **119** |
| Dump size | **3,389 lines / 175,156 bytes** | **3,389 lines / 175,156 bytes** |
| MD5 of the dump | `659f521033a44a4b95e57fe0bbe105a9` | `659f521033a44a4b95e57fe0bbe105a9` |

**`diff` exited 0 with zero differing lines**, and `cmp` reports the two dumps **byte-identical**.

One precision that matters, because the obvious worry about comparing two differently-named databases is
that a normalisation step could hide a real difference: **no normalisation was needed.** A single-database
`mysqldump --no-data --skip-comments --skip-dump-date` emits no `CREATE DATABASE` and no `USE` statement,
so the database name never appears in the output — verified as `grep -c "$DB_BEFORE" schema-before.sql`
→ **0**. The script still performs the name-normalisation substitution before diffing, but it is provably a
**no-op** here: the *raw*, un-normalised dumps already carry the identical MD5 above. Nothing in the DDL
text was rewritten, filtered or sorted.

Three safety statements, because this ran against a shared server:

- Only the two databases created for this comparison were ever written to. Their names are clone-scoped —
  `openmrs_v3_{before,after}_<suffix>`, where the suffix is the first eight hex digits of the MD5 of the
  checkout's absolute path, so two clones at the *same* commit still get distinct names; the recorded run
  used `openmrs_v3_before_f079886d` and `openmrs_v3_after_f079886d`. The script refuses to start if either
  composed name equals the shared database, and both are dropped by the cleanup trap.
- The shared `openmrs` database was measured **before** the run and **after** cleanup and was identical
  both times — **126 tables / 1,065 changesets** — with the comparison asserted by `cmp` inside the script
  rather than eyeballed. A neighbouring clone's database (`openmrs_c002`) was never referenced.
- Cleanup runs from a `trap … EXIT INT TERM`, so an interrupt or an early failure still drops both
  disposable databases and removes the credentials file.

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
`--url` per side, which (a) makes the target database impossible to inherit by accident, and (b) pins both
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

> **Destructive-command warning.** This procedure creates, drops and recreates databases. Every
> `DROP`/`CREATE` below is directed at a **name the script itself composes** from a clone-scoped suffix, and
> the script **refuses to start** if either name equals the shared database. Never substitute `openmrs`, and
> never point it at anything holding real data. The shared database is read **only** for the
> before/after assertion in steps 1 and 8.

This is the script that produced the measurements in (e).1 — not an outline of one. Four properties make
it safe to publish and safe to re-run:

- **Fail-fast with preserved status.** `set -euo pipefail`, and every step that can fail is wrapped in an
  `if ! …; then` guard that reports and exits non-zero. `diff`'s status is captured into `DIFF_STATUS`
  (with `set +e` around it, because a *non-zero* `diff` status is the failure signal, not a shell error)
  and the run's final verdict is derived from that captured value. **No later step can mask an earlier
  failure**, and cleanup cannot overwrite the verdict.
- **Isolation.** Both sides live in disposable, clone-scoped databases created by the script. The shared
  database is only ever read, and the pre/post readings are compared with `cmp` so drift fails the run.
- **Trap-based cleanup.** `trap cleanup EXIT INT TERM` drops both disposable databases and removes the
  credentials file even on interrupt or early exit, and it re-raises the original exit status.
- **Evidence retention.** Both raw dumps, the normalised dumps, the `diff` output, the MD5 list, the
  per-side metrics and both Liquibase logs are written under an evidence directory *before* any cleanup,
  so the artifacts behind every figure in (e).1 outlive the run.

Credentials go in a `--defaults-extra-file`, not on the command line. The `-u <user> -p<pw>` form that
looks natural in prose is **not executable**: the shell reads `<user>` and `>` as input/output
redirection, so the command fails before `mysqldump` ever starts. The file is created `chmod 600` and
deleted with `rm -f` — that is **removal, not secure erasure**; if a shredding guarantee is required on
the host in question, substitute `shred -u` (or keep the file on a `tmpfs`).

```bash
  #!/usr/bin/env bash
  set -euo pipefail

  BASE_COMMIT=3934d8086c684269e935562f25e806be91947115
  SUFFIX="$(printf '%s' "$PWD" | md5sum | cut -c1-8)"   # per-checkout; keeps parallel clones apart
  DB_BEFORE="openmrs_v3_before_$SUFFIX"       # disposable
  DB_AFTER="openmrs_v3_after_$SUFFIX"         # disposable
  SHARED_DB=openmrs                           # READ-ONLY here. Never a DROP/CREATE target.
  DB_HOST=127.0.0.1; DB_PORT=3306
  EVIDENCE=./v3-evidence; mkdir -p "$EVIDENCE"

  # 0. Credentials once, in a private file. Never inline them, never use -p<pw>.
  MYSQL_CNF="$(mktemp)"; chmod 600 "$MYSQL_CNF"
  printf '[client]\nuser=root\npassword=%s\nhost=%s\nport=%s\n' "$DB_PASS" "$DB_HOST" "$DB_PORT" > "$MYSQL_CNF"

  # Cleanup ALWAYS runs and NEVER changes the verdict: it preserves $? and re-exits with it.
  cleanup() {
    status=$?; set +e
    mysql --defaults-extra-file="$MYSQL_CNF" \
      -e "DROP DATABASE IF EXISTS \`$DB_BEFORE\`; DROP DATABASE IF EXISTS \`$DB_AFTER\`;" \
      >> "$EVIDENCE/cleanup.log" 2>&1
    rm -f "$MYSQL_CNF"          # removal, not secure erasure - see the note above
    exit "$status"
  }
  trap cleanup EXIT INT TERM

  # Guard: never operate on the shared database.
  for d in "$DB_BEFORE" "$DB_AFTER"; do
    [ "$d" = "$SHARED_DB" ] && { echo "FATAL: disposable name equals shared database" >&2; exit 1; }
  done

  # 1. Shared database pre-state - READ ONLY. Re-checked identical in step 8.
  mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e \
    "SELECT (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$SHARED_DB'), \
            (SELECT COUNT(*) FROM $SHARED_DB.liquibasechangelog);" > "$EVIDENCE/shared-db-before.txt"

  # 2. Materialise the BASE-COMMIT changelog bytes without touching the working tree,
  #    and prove byte-for-byte equality with the current tree.
  mkdir -p base-changelogs
  git archive "$BASE_COMMIT" -- 'api/src/main/resources/liquibase-*.xml' \
                                'api/src/main/resources/org/openmrs/liquibase' | tar -x -C base-changelogs
  BASE_ROOT=base-changelogs/api/src/main/resources
  diffcount=0
  while IFS= read -r f; do
    rel="${f#"$BASE_ROOT"/}"
    cmp -s "$f" "api/src/main/resources/$rel" || { echo "DIFFERS: $rel"; diffcount=$((diffcount+1)); }
  done < <(find "$BASE_ROOT" -name 'liquibase-*.xml' | sort)
  [ "$diffcount" -eq 0 ] || { echo "FATAL: changelog bytes are not frozen" >&2; exit 1; }

  # 3. Resolve the SAME engine for both sides: liquibase-core 4.32.0 off the api classpath.
  #    mdep.outputFile MUST be absolute: with -pl it is otherwise resolved against the
  #    module basedir and lands in api/, not here.
  CP_FILE="$PWD/api_cp.txt"
  ./mvnw -B -q -o -pl api dependency:build-classpath \
    -Dmdep.outputFile="$CP_FILE" -Dmdep.includeScope=runtime
  grep -q 'liquibase-core/4\.32\.0/' "$CP_FILE" || { echo "FATAL: wrong engine" >&2; exit 1; }
  CP="$(cat "$CP_FILE")"

  # 4-6. Install and dump each side. $1 = database, $2 = changelog resource root, $3 = label.
  run_side() {
    local db="$1" root="$2" label="$3"
    if ! mysql --defaults-extra-file="$MYSQL_CNF" -e \
      "DROP DATABASE IF EXISTS \`$db\`; CREATE DATABASE \`$db\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    then echo "FATAL: could not create $db" >&2; exit 1; fi

    # Explicit disposable URL - the liquibase/pom.xml <url> points at the SHARED database
    # and must never be used unoverridden. The changelog table names match DatabaseUpdater.
    if ! java -cp "$root:$CP" liquibase.integration.commandline.Main \
        --logLevel=warning --driver=com.mysql.cj.jdbc.Driver \
        --url="jdbc:mysql://$DB_HOST:$DB_PORT/$db" --username=root --password="$DB_PASS" \
        --databaseChangelogTableName=liquibasechangelog \
        --databaseChangelogLockTableName=liquibasechangeloglock \
        --changeLogFile=liquibase-schema-only.xml update > "$EVIDENCE/liquibase-update-$label.log" 2>&1
    then echo "FATAL: liquibase update failed for $label" >&2; exit 1; fi

    mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e "
      SELECT 'tables',      COUNT(*) FROM information_schema.tables  WHERE table_schema='$db' AND table_type='BASE TABLE'
      UNION ALL SELECT 'columns',    COUNT(*) FROM information_schema.columns WHERE table_schema='$db'
      UNION ALL SELECT 'indexes',    COUNT(DISTINCT CONCAT(table_name,'.',index_name)) FROM information_schema.statistics WHERE table_schema='$db'
      UNION ALL SELECT 'fkeys',      COUNT(*) FROM information_schema.table_constraints WHERE table_schema='$db' AND constraint_type='FOREIGN KEY'
      UNION ALL SELECT 'changesets', COUNT(*) FROM \`$db\`.liquibasechangelog;" > "$EVIDENCE/metrics-$label.txt"

    if ! mysqldump --defaults-extra-file="$MYSQL_CNF" --no-data --skip-comments --skip-dump-date \
        "$db" > "$EVIDENCE/schema-$label.sql"
    then echo "FATAL: mysqldump failed for $db" >&2; exit 1; fi
  }
  run_side "$DB_BEFORE" "$BASE_ROOT"              before
  run_side "$DB_AFTER"  api/src/main/resources    after

  # 7. Compare. The name substitution is a documented NO-OP for a single-database dump
  #    (no CREATE DATABASE / USE is emitted); it is kept only so the step is explicit.
  sed "s/\`$DB_BEFORE\`/\`DBNAME\`/g" "$EVIDENCE/schema-before.sql" > "$EVIDENCE/schema-before.norm.sql"
  sed "s/\`$DB_AFTER\`/\`DBNAME\`/g"  "$EVIDENCE/schema-after.sql"  > "$EVIDENCE/schema-after.norm.sql"
  set +e
  diff "$EVIDENCE/schema-before.norm.sql" "$EVIDENCE/schema-after.norm.sql" > "$EVIDENCE/schema.diff"
  DIFF_STATUS=$?          # captured, asserted in step 9 - never discarded
  set -e
  md5sum "$EVIDENCE"/schema-*.sql > "$EVIDENCE/checksums.txt"

  # 8. Shared database post-state must equal the pre-state. Asserted, not eyeballed.
  mysql --defaults-extra-file="$MYSQL_CNF" -N -B -e \
    "SELECT (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$SHARED_DB'), \
            (SELECT COUNT(*) FROM $SHARED_DB.liquibasechangelog);" > "$EVIDENCE/shared-db-after.txt"
  cmp -s "$EVIDENCE/shared-db-before.txt" "$EVIDENCE/shared-db-after.txt" \
    || { echo "FATAL: the shared database changed" >&2; exit 1; }

  # 9. Verdict from the CAPTURED status. Expected: 0, with an empty schema.diff.
  [ "$DIFF_STATUS" -eq 0 ] || { echo "FAIL: schemas differ - see $EVIDENCE/schema.diff" >&2; exit 1; }
  echo "PASS: schemas identical"
```

`$DB_PASS` is supplied by the operator from the environment (the containerised development server in
`docker-compose.yml` uses the project's default), so no credential is written into this document.

The run leaves three byproducts in the working tree — `api_cp.txt`, `base-changelogs/` and the
`v3-evidence/` directory. The first two are scratch; the third is the retained evidence the figures in
(e).1 come from. None of them belongs in a commit, so delete them (or add them to a local exclude) once
the evidence has been read.

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
  ./mvnw verify -Pintegration-test -B     # documented, NOT executed here
  ./mvnw verify -Pperformance-test -B     # documented, NOT executed here
```

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

Every edit below carries a named attribution to the Spring 6+/Hibernate 6+/Jakarta/Java 21 target, as
Rule 1 and TR1 require. Nineteen files were in the UPDATE set and one file — this document — was created.

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
conversion is explicitly **excluded**, and only the `xsi:schemaLocation` attribute changed:

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
  ./mvnw verify -Pintegration-test -B          # documented, NOT executed here
  ./mvnw verify -Pperformance-test -B          # documented, NOT executed here

  # V3 - clean-database Liquibase run and mysqldump --no-data diff
  #      EXECUTED. Both sides installed by liquibase-core 4.32.0 into their OWN disposable,
  #      clone-scoped database with an explicit --url per side - the "before" side from
  #      `git archive 3934d8086`-extracted changelog bytes, cmp-verified 38 identical /
  #      0 differing - then dumped with
  #        mysqldump --no-data --skip-comments --skip-dump-date
  #      and compared. Result: diff exit status 0, zero differing lines, both dumps
  #      byte-identical at MD5 659f521033a44a4b95e57fe0bbe105a9, 119 tables / 1,510
  #      columns / 697 indexes / 446 foreign keys / 1,028 changesets on each side, and the
  #      shared `openmrs` database asserted unchanged at 126 tables / 1,065 changesets.
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

  # A2  NOTICE.md attribution is truthful: nothing attributed that is no longer
  #     shipped, while commons-collections, liquibase-core, the Infinispan entries,
  #     jakarta.xml.bind-api, jaxb-runtime and type-converter all remain.

  # A3  Spring contexts still load - exercised by every context-sensitive test in
  #     the V2 run. Expect no BeanDefinitionParsingException and no XSD resolution failure.

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

  # A6  no transformation or shading plugin was introduced (Rule 2): still exactly
  #     the 23 managed plugins, no transformer/shade/relocate.

  # A7  frozen artifacts untouched. Compare COMMITTED state against the base commit -
  #     a worktree-only `git diff` / `git status` reports nothing at a clean HEAD and
  #     therefore cannot verify a committed change at all.
  BASE=3934d8086c684269e935562f25e806be91947115
  git diff --name-status "$BASE"..HEAD -- \
    'api/src/main/resources/liquibase-*.xml' \
    'api/src/main/resources/org/openmrs/liquibase/**' \
    'api/src/main/resources/**/*.hbm.xml' \
    api/src/main/resources/hibernate.cfg.xml \
    api/src/main/java/org/openmrs/aop/AOPConfig.java \
    api/src/main/java/org/openmrs/aop/AuthorizationAdvice.java \
    webapp/src/main/webapp/WEB-INF/web.xml                         # expect NO output
  # Prove the gate is not vacuous - a pathspec that matches nothing also prints nothing:
  git ls-files -- 'api/src/main/resources/liquibase-*.xml' \
    'api/src/main/resources/org/openmrs/liquibase/**' \
    'api/src/main/resources/**/*.hbm.xml' | wc -l                  # expect a non-zero count

  # A8  build wall clock shows no change of ORDER. Read Maven's own "Total time" line
  #     rather than an external stopwatch, and treat it as a sanity check, not a
  #     benchmark: neither the pre- nor the post-change run controlled for CPU
  #     contention or repository warmth, so a minutes-vs-minutes comparison is all
  #     that is supportable. Measured here: install 01:21 min, test 09:53 min.
  grep -E '^\[INFO\] Total time' install.log test.log

  # A9  formatting conforms. The -D flag is LOAD-BEARING: the root pom.xml sets
  #     <spotless.check.skip>true</spotless.check.skip> by default and only the
  #     ci-checks profile flips it, so a bare `./mvnw spotless:check` prints
  #     "Spotless check skipped" for every module and STILL EXITS 0 - a vacuous pass.
  ./mvnw spotless:check -B -Dspotless.check.skip=false      # expect BUILD SUCCESS, 0 violations

  # A10 this document exists and is complete
  test -f doc/JAKARTA_MIGRATION_BASELINE.md && echo present
```

All of A1 through A10 were run against the changed tree and all passed. A4, A5 and A7 return zero over
tracked source, with the build-output caveat on A5 recorded in the block above rather than left for a
future reader to trip over; A9 reports `BUILD SUCCESS` with no file reformatted, using the explicit
`-Dspotless.check.skip=false` that makes the goal actually execute; and A8 is satisfied in the only sense
the evidence supports — `install` **01:21 min** and `test` **09:53 min**, both minutes-scale and both
exiting 0, with **no** speed comparison drawn against the planning figures, for the reason set out in
section (a).1.

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

## Definition of Done

- [x] `./mvnw clean install -DskipTests -B` exits **0** with all **13** reactor projects building.
- [x] `./mvnw test -B` reports exactly **5,106 run / 0 failures / 0 errors / 45 skipped**, with no
      assertion modified, no test deleted and no new `@Disabled`.
- [x] `./mvnw dependency:tree -B` shows **zero `javax.*` dependency-tree nodes** — not merely zero
      `servlet` and `persistence` nodes.
- [x] `grep -rE "import javax\.(servlet|persistence|validation|annotation|transaction)"` returns zero hits.
- [x] The three removed coordinates are absent from the graph and from `NOTICE.md`, and no other
      attribution was disturbed.
- [x] No file under the frozen sets appears in `git diff --name-status "$BASE"..HEAD` for the frozen
      pathspecs (the committed-range form; a worktree-only `git diff` proves nothing at a clean HEAD).
- [x] This document exists and carries the full evidence trail, including the executed section (e)
      comparison and the phase-labelled environment history behind it.
- [x] Every UPDATE is traceable to a named target-stack justification, and every one of the seven
      pre-existing defect register items is **left unfixed and recorded** — items 1-5 with an in-place
      comment at their own site, items 6 and 7 in the register only, because the 18 locale catalogues and
      the byte-identical verify-only root `pom.xml` are not files this change may annotate. See the
      "where each item is recorded" table in the Pre-Existing Defect Register.
- [x] The clean-database Liquibase run and `mysqldump --no-data` schema diff — **executed**, not merely
      documented: two disposable, clone-scoped databases installed by `liquibase-core` 4.32.0 from
      base-commit and current changelog bytes, both dumped and compared. **`diff` exit status 0, zero
      differing lines, both dumps byte-identical at MD5 `659f521033a44a4b95e57fe0bbe105a9`
      (3,389 lines / 175,156 bytes each), 119 tables / 1,510 columns / 697 indexes / 446 foreign keys /
      1,028 changesets on each side, and the shared `openmrs` database asserted unchanged at
      126 tables / 1,065 changesets.** The runnable script, its fail-fast/trap/isolation properties and the
      retained evidence files are in section (e).4.

## Further Reading

- [`doc/JUNIT5_MIGRATION.md`](JUNIT5_MIGRATION.md) — the companion migration guide, and the document
  pattern this file follows
- [`NOTICE.md`](../NOTICE.md) — third-party attribution, kept in step with every dependency removal
- [`bom/pom.xml`](../bom/pom.xml) — the single version and exclusion control point (TR3)
- [Jakarta EE Specifications](https://jakarta.ee/specifications/)
- [Hibernate ORM 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/main/migration-guide.adoc)
- [Spring Framework 6 Upgrade Notes](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x)
- [OpenMRS Wiki](https://wiki.openmrs.org/)
