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

| Measurement | Pre-change (planning capture) | Post-change (measured here) |
|---|---|---|
| `clean install -DskipTests` | `BUILD SUCCESS`, 2 min 16 s, 13 of 13 reactor projects | `BUILD SUCCESS`, **1 min 19 s**, **13 of 13** reactor projects, exit code **0** |
| `test` | `BUILD SUCCESS`, 10 min 17 s | `BUILD SUCCESS`, **10 min 17 s**, exit code **0** |

> **Note:** the post-change `install` wall clock is *faster* than the pre-change figure (1:19 against
> 2:16), not slower. The difference is environmental — the local Maven repository was already fully
> warmed, so nothing was downloaded. It is reported as measured rather than normalised to the earlier
> number, and it satisfies the "no material build-time regression" gate in the reproduction section.

### 2. Invariants That Held

- Surefire runs with **`testFailureIgnore=false`**, so a 100% pass rate is a hard gate — any failure
  fails the build.
- Because the baseline has **zero failures, zero test exclusions are permissible — there is nothing
  to grandfather.** The requirement's allowance for "exclusions limited to failures present at
  baseline" therefore evaluates to an allowance of **nothing**, and none was taken.
- **No assertion was weakened, no test was deleted, and no new `@Disabled` was added.** The only path
  under any `src/test/` tree that this change touches is `webapp/src/test/resources/override-web.xml`,
  a servlet-container **resource** descriptor, not a test. **Zero Java test files were modified.**
- The **45 skips** trace to **42 `@Disabled` annotations across 22 `api` test classes**, 6 of them
  class-level. This was verified at both ends: the `@Disabled` occurrence count is **42** at the base
  commit and **42** at the changed tree, so the ceiling did not grow.
- Surefire's global `argLine` is load-bearing on Java 21 and was not altered. It is *composed* rather
  than literal — the root `pom.xml` declares
  `-Duser.language=en -Duser.region=US -Xmx1g ${customArgLineForTesting} -Djava.locale.providers=COMPAT`
  and sets `customArgLineForTesting` to
  `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`, so the
  effective value carries both `--add-opens` flags and the `COMPAT` locale providers.

### 3. What This Capture Also Serves As

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

The exclusion is source-neutral because `api/pom.xml` already declares the Jakarta replacements —
`jakarta.xml.bind:jakarta.xml.bind-api` and `org.glassfish.jaxb:jaxb-runtime`. No Java file lost a
capability.

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

**Why the observable failure mode is identical:** both exception types are **unchecked**, and both funnel
through the OpenMRS-owned hierarchy — `DAOException extends APIException`, which `extends
RuntimeException`. **No test asserts on the concrete type**, and the suite is 100% green. This is
recorded as a Rule 6 deviation with preserved observable behaviour, **not** as a defect to repair.

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
| `com.fasterxml.jackson.core:jackson-annotations` `2.21` | **kept as pinned** | the missing patch component is **deliberate and correct, not a skew**: `2.21.2` returns HTTP 404 on Maven Central while `2.21` returns HTTP 200. The BOM carries an inline comment recording this |
| All 23 managed Maven plugins | **unchanged** | every one executed successfully on JDK 21 across both a full `install` and a full `test` run, so no plugin bump is attributable |

### 6. Research Method

`web_search` returned no usable results for the intended version queries, so no external version claim
in this change rests on it. All external version facts were obtained from **primary registry endpoints**
— Maven Central's `maven-metadata.xml` and `.pom` endpoints under `https://repo1.maven.org/maven2/`,
plus the OpenMRS Nexus repository declared in the reactor, which is where the BOM-only private
coordinate `org.openmrs.liquibase.ext:type-converter:1.0.1` resolves from. **No version claim is
asserted from memory.**

## Section (e): Clean-Database Liquibase Run and Schema Diff

### 1. This Is a Procedure, Not an Executed Result

**The clean-database Liquibase run and the `mysqldump --no-data` schema comparison were not executed as
part of this change.** Nothing in this section should be read as a report of an observed diff.

The environmental constraint deserves stating precisely, because it differs between the environment
where this change was planned and the environment where it was carried out:

- In the **planning environment** no `mysql`, `mysqld` or `mysqldump` binary was present at all, and
  neither were `mariadb` or `mariadb-dump`. That is why the item was specified as a procedure.
- In the **execution environment** the client tooling *is* present — `mysql`, `mysqldump`, `mariadb` and
  `mariadb-dump` all resolve, from a MariaDB 11.8.3 client package — and a `mariadb:10.11.7` server is
  reachable on `127.0.0.1:3306`. Only the server binaries `mysqld` and `mariadbd` are absent, because
  the server runs in a container.

So the *tooling* gap has closed, and this document does not repeat the claim that it has not. The diff
was nonetheless not performed, for two substantive reasons:

1. A genuine **pre-change** capture must come from a clean database brought up by the changelogs *as
   they were before the change*. Producing one requires checking out the base commit, which is outside
   what this change may do to the working tree.
2. A **stronger and directly executable proof is available and was executed instead** — the changelog
   inputs are provably byte-identical before and after, which is established below and which implies an
   empty diff rather than sampling for one.

### 2. The Harness Already Exists

No further design work is needed by whoever runs it. The `liquibase-maven-plugin` configuration in
`liquibase/pom.xml` already supplies:

- `<driver>com.mysql.cj.jdbc.Driver</driver>`
- `<url>jdbc:mysql://127.0.0.1:3306/openmrs</url>`
- `<changeLogFile>snapshots/${changelogfile}</changeLogFile>`
- `<diffTypes>${diffTypes}</diffTypes>` and `<outputChangeLogFile>snapshots/${outputChangelogfile}</outputChangeLogFile>`

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

```bash
  # 1. Capture the baseline schema BEFORE the change, from a clean database that has
  #    been brought up by the UNMODIFIED changelogs.
  mysqldump --no-data --skip-comments -h 127.0.0.1 -u <user> -p<pw> openmrs > baseline-schema.sql

  # 2. Drop and recreate the database, then apply the changelogs AFTER the change,
  #    using the existing plugin configuration in liquibase/pom.xml
  #    (driver com.mysql.cj.jdbc.Driver, url jdbc:mysql://127.0.0.1:3306/openmrs).

  # 3. Capture the post-change schema exactly the same way.
  mysqldump --no-data --skip-comments -h 127.0.0.1 -u <user> -p<pw> openmrs > postchange-schema.sql

  # 4. Compare. Expected: EMPTY.
  diff baseline-schema.sql postchange-schema.sql
```

### 5. Why the Diff Is Empty by Construction

Four independent reasons, each verified:

**Reason 1 — every changelog file is frozen byte-for-byte.** `git diff` against the base commit reports
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
the sanctioned replacement for the removed `org.hibernate.type.EnumType`. Exactly **three** mapping sites
consume it: `Obs.hbm.xml` twice and `OrderSet.hbm.xml` once. The only edit to that file in this change is
a comment.

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
`OrderValidator.java`, `DrugOrderValidator.java` and `StringEnumType.java`. They are itemised in the
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
so none of them was fixed.** Each is annotated in a comment at its own site.

| # | Site | Condition | Attributable to the target stack? |
|---|---|---|---|
| 1 | `api/src/main/resources/hibernate.default.properties` | `hibernate.connection.driver_class=com.mysql.jdbc.Driver` is the deprecated legacy driver class name; MySQL Connector/J 9.7.0 emits a deprecation notice on every test run. The modern name is `com.mysql.cj.jdbc.Driver`, which `liquibase/pom.xml` already uses | **No** — JDBC driver naming. Comment only; **the value was not changed** |
| 2 | `api/src/main/resources/infinispan-api-local.xml` | malformed root start-tag with a **doubled `>`**: `xmlns="urn:infinispan:config:15.2">>`. The well-formed sibling is `infinispan-api.xml` | **No**. Comment only; **not fixed** |
| 3 | `api/src/main/java/org/openmrs/validator/OrderValidator.java` | a comment cites `Order.hbm.xml`, which **does not exist** — only `OrderFrequency`, `OrderSet`, `OrderSetAttribute` and `OrderSetMember` HBM files exist; `Order` is annotation-mapped | **No**. Comment correction only |
| 4 | `api/src/main/java/org/openmrs/validator/DrugOrderValidator.java` | the identical stale `Order.hbm.xml` citation | **No**. Comment correction only |
| 5 | `api/src/main/java/org/openmrs/api/db/hibernate/type/StringEnumType.java` | the javadoc says the class should be deleted once `Obs`, `ConceptName` and `OrderSet` move from HBM to annotations, but `ConceptName` is **already annotated**, so the stated precondition is only partly satisfied | **No**. Comment correction only. **This file was promoted from inspected-only to edited solely because of Rule 5** — a directly traceable consequence of the rule |
| 6 | 18 locale files, 2 keys each, e.g. `api/src/main/resources/messages.properties` | orphaned i18n keys for `org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler`, **a class with zero `.java` files** in the repository | **No** — **out of scope entirely**; deleting message keys is not attributable |
| 7 | root `pom.xml` versus `bom/pom.xml` | `liquibase-maven-plugin` **4.33.0** against `liquibase-core` **4.32.0**. The plugin runs only in the `openmrs-liquibase` module's manual snapshot-generation configuration, never in the default lifecycle, so the skew cannot affect the build or the changelogs — and neither version removes the `jaxb-api` leak | **No**. Documented, not changed |

### 1. Citing These Defects by Content, Not by Line

The annotation comments added for items 1, 2 and 5 shifted the lines they describe. The line references
recorded during planning were correct **at the base commit** but no longer point at the same text: the
deprecated `driver_class` moved from L6 to L7, the doubled `>>` from L15 to L16,
`hibernate.cache.region.factory_class` from L30 to L31, and the `StringEnumType` class declaration from
L38 to L44. These sites are therefore cited by content throughout this document. Line numbers move;
identifiers and text do not.

### 2. The Contrast That Makes Rules 1 and 5 Operable Together

**Three findings were attributable, and were therefore changed:**

1. the stale `org.springframework.orm.hibernate3` javadoc, because it names a package removed in the
   target generation;
2. the five legacy Spring XSD version pins, because 2.5 and 3.0 grammars misdescribe a Spring 7 classpath;
3. the `javax`-era `override-web.xml` root element, superseded by the Jakarta descriptor generation.

Everything else discovered in passing was **annotated where it lives and left unfixed**. That is the
distinction between Rule 1 and Rule 5 in practice: attribution decides whether a finding is repaired or
merely recorded.

## Behavioural-Preservation Evidence

This is a medical-records platform, so behavioural preservation is the point of the exercise. An upgrade
that changes what a service method returns, what validation rejects, or what a privilege check permits has
failed regardless of a green build.

### 1. The Frozen Advisor Chain

`api/src/main/java/org/openmrs/aop/AOPConfig.java` was **not edited** — `git diff` against the base commit
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

There is **no Spring `PersistenceExceptionTranslator` or `HibernateExceptionTranslator` wired anywhere** —
re-measured here as zero files referencing either. Instead the DAO layer declares **`throws DAOException`
590 times** across `api/src/main/java/org/openmrs/api/db`, and `DAOException extends APIException`, which
`extends RuntimeException`.

Because the entire hierarchy is **unchecked and OpenMRS-owned**, Jakarta and Hibernate exception-type churn
**cannot alter a single declared signature**. This is the structural reason the requirement's
*"`org.openmrs.api.*` signatures remain identical"* clause holds **without any compatibility shim**, and it
is why the `NonUniqueResultException` deviation in section (d) is invisible at the service boundary.

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
obligation. Legacy residue is gone: **zero** usages of `org.hibernate.Criteria`, `org.hibernate.criterion`
or `org.hibernate.transform` remain.

### 5. DAO Structure Preserved

Rule 4 forbids restructuring the data-access layer, and the proof is a diff rather than an inventory:
`git diff` against the base commit for `api/src/main/java/org/openmrs/api/db/` reports **3 files changed,
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
  #      DOCUMENTED PROCEDURE, NOT EXECUTED. See section (e).

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

  # A4/A5 note: scope these two greps to TRACKED files. The bare recursive form
  #   grep -rn "java.sun.com/xml/ns/javaee" --include=*.xml .
  # returns 0 on a clean checkout but 1 on a tree that has been built, because
  # web/target/spotbugsXml.xml - generated build output, untracked and gitignored -
  # quotes the string in a report entry. That single hit is an artifact of the
  # report, not a surviving descriptor; both web.xml and override-web.xml are on
  # https://jakarta.ee/xml/ns/jakartaee. The gate is about source, so it must
  # measure source.

  # A6  no transformation or shading plugin was introduced (Rule 2): still exactly
  #     the 23 managed plugins, no transformer/shade/relocate.

  # A7  frozen artifacts untouched
  git diff --name-only    # expect no path under api/src/main/resources/org/openmrs/liquibase/,
                          # no liquibase-*.xml, no .hbm.xml, no AOPConfig.java, no initial_test_db.sql

  # A8  build wall clock not materially regressed against 2:16 and 10:17

  # A9  formatting conforms
  ./mvnw spotless:check -B                                                 # expect BUILD SUCCESS

  # A10 this document exists and is complete
  test -f doc/JAKARTA_MIGRATION_BASELINE.md && echo present
```

All of A1 through A10 were run against the changed tree and all passed. A4, A5 and A7 return zero over
tracked source, with the build-output caveat on A5 recorded in the block above rather than left for a
future reader to trip over; A9 reports `BUILD SUCCESS` with no file reformatted; A8 is satisfied with the
`install` phase measurably faster and `test` identical, as recorded in section (a).

## Definition of Done

- [x] `./mvnw clean install -DskipTests -B` exits **0** with all **13** reactor projects building.
- [x] `./mvnw test -B` reports exactly **5,106 run / 0 failures / 0 errors / 45 skipped**, with no
      assertion modified, no test deleted and no new `@Disabled`.
- [x] `./mvnw dependency:tree -B` shows **zero `javax.*` dependency-tree nodes** — not merely zero
      `servlet` and `persistence` nodes.
- [x] `grep -rE "import javax\.(servlet|persistence|validation|annotation|transaction)"` returns zero hits.
- [x] The three removed coordinates are absent from the graph and from `NOTICE.md`, and no other
      attribution was disturbed.
- [x] No file under the frozen sets appears in `git diff --name-only`.
- [x] This document exists and carries the full evidence trail, including the section (e) procedure and the
      environmental facts that kept it from being executed.
- [x] Every UPDATE is traceable to a named target-stack justification, and every one of the seven
      pre-existing defect register items is annotated in a comment at its site and left unfixed.
- [ ] The clean-database Liquibase run and `mysqldump --no-data` schema diff — **documented as a procedure,
      deliberately not executed here.** Left unchecked on purpose; see section (e).

## Further Reading

- [`doc/JUNIT5_MIGRATION.md`](JUNIT5_MIGRATION.md) — the companion migration guide, and the document
  pattern this file follows
- [`NOTICE.md`](../NOTICE.md) — third-party attribution, kept in step with every dependency removal
- [`bom/pom.xml`](../bom/pom.xml) — the single version and exclusion control point (TR3)
- [Jakarta EE Specifications](https://jakarta.ee/specifications/)
- [Hibernate ORM 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/main/migration-guide.adoc)
- [Spring Framework 6 Upgrade Notes](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x)
- [OpenMRS Wiki](https://wiki.openmrs.org/)
