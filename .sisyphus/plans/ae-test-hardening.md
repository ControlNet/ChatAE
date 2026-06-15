# AE Extension Test Hardening Plan

## TL;DR
> **Summary**: Harden AE extension testing by first enforcing shared-scenario GameTest parity across Fabric and Forge, then adding targeted JUnit/regression coverage around AE terminal logic, and finally rolling AE Forge runtime coverage into CI with a progressive nightly-first policy.
> **Deliverables**:
> - Shared AE GameTest parity audit and loader exposure fixes
> - High-value regression suites for `AiTerminalPartOperations`, resolver, renderer, and AE registration wiring
> - Targeted runtime/GameTest additions for part lifecycle and binding/rebind edge cases
> - Nightly AE Forge GameTest workflow wiring plus synchronized testing docs and evidence expectations
> **Effort**: Large
> **Parallel**: YES - 3 waves
> **Critical Path**: 1 → 2/3/4/5 → 6/7/8/9/10 → 11/12/13 → 14/15

## Context
### Original Request
- Continue AE extension development from a stronger testing baseline because current coverage feels insufficient.
- Include both GameTest improvements and broader AE test hardening work.
- Keep AE GameTests aligned with the base-mod architecture: shared logic in common code, thin Fabric/Forge wrappers only.

### Interview Summary
- Existing AE test coverage is real but uneven: provider/policy/proposal/data contracts are better protected than AE terminal/runtime integration behavior.
- Base mod already demonstrates the target GameTest architecture: shared scenario logic in `base/common-1.20.1/.../gametest/*Scenarios.java`, thin Fabric entrypoints/runtime delegates, and thin Forge wrappers/bootstrap registration.
- ext-ae mostly mirrors that structure via `AeCraftLifecycleIsolationGameTestScenarios` and `AeBindingFailureGameTestScenarios`, but the mapping is incomplete: `AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` exists in common, Fabric has a runtime delegate, but Fabric entrypoint and Forge wrapper/bootstrap do not fully expose it.
- Existing AE JUnit tests cover `AeToolProvider`, `AeToolPolicy`, `AeProposalFactory`, `AiTerminalData`, renderer shape checks, and model-id constants.
- Existing AE runtime tests cover five main smoke scenarios, but the thinner areas remain `AiTerminalPartOperations`, `AiTerminalPart`, `AeTerminalContextResolver`, richer renderer formatting, and CI parity for AE Forge runtime.

### Metis Review (gaps addressed)
- This is a testing-architecture hardening task, not a generic “increase coverage” task.
- Shared AE GameTest registration parity is treated as a first-class acceptance target, not a side note.
- The plan is bounded to four workstreams only: GameTest architecture parity, high-value JUnit/regression additions, targeted runtime/integration coverage, and CI rollout.
- Progressive CI rollout is the default: keep PR/dev JUnit unchanged, add AE Forge GameTests to nightly first, and do not introduce PR gating in this plan.
- Acceptance criteria must prove concrete commands, loader exposure points, and workflow wiring rather than vague “coverage improved” claims.

## Work Objectives
### Core Objective
Make AE extension testing trustworthy enough for continued development by enforcing base-mod-style shared GameTest architecture, closing the most valuable regression gaps in AE terminal logic, and introducing AE Forge runtime coverage into CI without destabilizing fast feedback lanes.

### Deliverables
- Shared AE GameTest scenario inventory with complete Fabric/Forge exposure for in-scope shared scenarios
- Loader-thin AE GameTest structure consistent with base-mod patterns
- New regression suites for `AiTerminalPartOperations`, `AeTerminalContextResolver`, `AeToolOutputRenderer`, and AE init/registration wiring
- New runtime/GameTest coverage for AE part lifecycle, binding/rebind success/failure, and request/cancel/status edge cases
- Nightly AE Forge GameTest workflow wiring, artifacts, and docs parity (`REPO.md`, `docs/layered-testing-ci.md`)

### Definition of Done (verifiable conditions with commands)
- `./gradlew --no-daemon --configure-on-demand :ext-ae:core:test` exits `0` after the new regression coverage lands.
- `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test` exits `0` and includes the new AE regression classes.
- `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke` exits `0` with the in-scope shared AE scenarios exposed through Fabric.
- `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace` exits `0` with the same in-scope shared AE scenarios exposed through Forge.
- `.github/workflows/layered-testing.yml` contains the AE Forge GameTest command in the chosen nightly rollout lane and does not add PR gating.
- `REPO.md` and `docs/layered-testing-ci.md` reflect the final AE runtime lane behavior and commands.

### Must Have
- Shared-scenario-only AE GameTest architecture for all in-scope runtime additions
- Full loader exposure for every in-scope shared AE GameTest scenario, including the known orphaned binding-unavailable path
- High-value deterministic regression coverage for `AiTerminalPartOperations`
- Targeted observable-behavior coverage for `AeTerminalContextResolver`, `AiTerminalPart`, and `AeToolOutputRenderer`
- Progressive AE Forge CI rollout in nightly only for this plan

### Must NOT Have (guardrails, AI slop patterns, scope boundaries)
- No loader-specific duplication of scenario bodies to “quick-fix” coverage
- No broad refactor of AE production architecture unrelated to testability
- No repo-wide test framework modernization or coverage tooling overhaul
- No changes to base-mod or ext-matrix test backlog except as pattern references
- No PR gating for AE Forge GameTests in this plan
- No unrelated workflow or release automation cleanup

## Verification Strategy
> ZERO HUMAN INTERVENTION - all verification is agent-executed.
- Test decision: tests-after with existing JUnit 5 + existing Fabric/Forge GameTest infrastructure; use targeted RED/GREEN sequencing inside each new regression area.
- QA policy: Every task includes both a happy path and a failure/edge path with explicit commands or source-validation checks.
- Evidence: `.sisyphus/evidence/task-{N}-{slug}.{ext}`

## Execution Strategy
### Parallel Execution Waves
> Target: 5 tasks per wave. Shared-scenario parity and CI rollout stay bounded; do not broaden into a full AE test overhaul.

Wave 1: shared AE GameTest parity and loader-thin architecture alignment (Tasks 1-5)
Wave 2: deterministic AE JUnit/regression hardening (Tasks 6-10)
Wave 3: targeted runtime/integration expansion plus CI rollout (Tasks 11-15)

### Dependency Matrix (full, all tasks)
- 1 → blocks 2, 3, 4, 5, 11, 12, 13
- 2 → blocks 4, 5, 14
- 3 → blocks 4, 5, 14
- 4 → blocks 5, 14
- 5 → blocks 14, 15
- 6 → blocks 7, 11, 13
- 7 → blocks 11, 13
- 8 → blocks 12
- 9 → blocks 13
- 10 → blocks 15
- 11 → blocks 14, 15
- 12 → blocks 14, 15
- 13 → blocks 14, 15
- 14 → blocks F1, F3, F4
- 15 → blocks F1, F2, F4

### Agent Dispatch Summary (wave → task count → categories)
- Wave 1 → 5 tasks → `deep`, `quick`, `quick`, `unspecified-high`, `quick`
- Wave 2 → 5 tasks → `deep`, `deep`, `unspecified-high`, `deep`, `quick`
- Wave 3 → 5 tasks → `deep`, `deep`, `unspecified-high`, `quick`, `writing`

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task MUST have: Agent Profile + Parallelization + QA Scenarios.

- [x] 1. Inventory shared AE GameTest scenarios and freeze the parity contract

  **What to do**: Build a finite inventory of the shared AE GameTest scenario methods currently housed in `AeCraftLifecycleIsolationGameTestScenarios` and `AeBindingFailureGameTestScenarios`, then define the in-scope parity contract for this plan: every shared scenario that remains in those common classes must be surfaced through both Fabric and Forge. If any method is intentionally excluded from loader exposure, move it out of the shared scenario surface or document it in code as helper-only rather than leaving it as an orphaned “runnable” scenario. Keep the source of truth in common code; do not create a parallel loader-only scenario catalog.
  **Must NOT do**: Do not add new business logic to loader modules. Do not introduce a generic repo-wide scenario registry framework. Do not broaden the inventory beyond ext-ae.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this task decides the architectural contract for every downstream AE GameTest change.
  - Skills: `[]` - No extra skill is required beyond repository analysis and disciplined edits.
  - Omitted: `["playwright", "github-cli"]` - No browser or remote GitHub work is needed.

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: 2, 3, 4, 5, 11, 12, 13 | Blocked By: none

  **References**:
  - Pattern: `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/ProposalBindingUnavailableGameTestScenarios.java:35-174` - base stores shared GameTest behavior in common scenario classes.
  - Pattern: `base/fabric-1.20.1/src/main/java/space/controlnet/mineagent/fabric/gametest/MineAgentFabricRuntimeGameTests.java:21-107` - Fabric runtime delegates stay thin and point into shared scenarios.
  - Pattern: `base/forge-1.20.1/src/main/java/space/controlnet/mineagent/forge/gametest/MineAgentGameTestBootstrap.java:10-36` - Forge registers thin wrappers rather than embedding scenario logic.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:44-256` - first shared AE scenario class to inventory.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:51-174` - second shared AE scenario class and source of the known orphaned scenario.

  **Acceptance Criteria**:
  - [ ] The common AE GameTest surface is finite and explicitly enumerated in code touched by this task.
  - [ ] Every in-scope shared AE scenario is assigned a Fabric exposure path and a Forge exposure path.
  - [ ] No runnable shared scenario remains orphaned after this inventory is established.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Shared AE scenario inventory is complete
    Tool: Grep
    Steps: Search `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest` for `public static void` and confirm each runnable scenario is represented in the task’s final parity contract.
    Expected: Every runnable shared scenario method is accounted for exactly once.
    Evidence: .sisyphus/evidence/task-1-ae-scenario-inventory.md

  Scenario: No orphaned shared scenario remains
    Tool: Grep
    Steps: Search Fabric and Forge GameTest entry surfaces for every in-scope shared scenario method name from the inventory.
    Expected: Each method name appears in both loader exposure paths, or it has been converted into a non-runnable helper-only method by design.
    Evidence: .sisyphus/evidence/task-1-ae-scenario-parity.md
  ```

  **Commit**: NO | Message: `test(ext-ae): align shared gametest parity across loaders` | Files: `ext-ae/common-1.20.1/**`, `ext-ae/fabric-1.20.1/**`, `ext-ae/forge-1.20.1/**`

- [x] 2. Expose the orphaned binding-unavailable shared scenario through Fabric

  **What to do**: Surface `AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` through the existing Fabric AE entry chain. Keep the structure identical to base: `MineAgentAeFabricGameTestEntrypoint` provides the `@GameTest`, filter participation, and AE-runtime availability guard; `MineAgentAeFabricRuntimeGameTests` remains a thin delegate only. Place the new Fabric GameTest in the AE smoke family so nightly Fabric coverage sees it without inventing a new lane.
  **Must NOT do**: Do not copy the scenario body into Fabric. Do not introduce loader-specific assertions. Do not bypass `GameTestRuntimeLease` or existing AE-runtime gating.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: this is a focused Fabric wrapper/entrypoint alignment change.
  - Skills: `[]` - Existing repo patterns are sufficient.
  - Omitted: `["playwright"]` - Not a browser task.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 4, 5, 14 | Blocked By: 1

  **References**:
  - Pattern: `base/fabric-1.20.1/src/main/java/space/controlnet/mineagent/fabric/gametest/MineAgentFabricGameTestEntrypoint.java:60-67` - base Fabric exposes proposal-binding failure through a thin entrypoint method.
  - Pattern: `base/fabric-1.20.1/src/main/java/space/controlnet/mineagent/fabric/gametest/MineAgentFabricRuntimeGameTests.java:60-65` - base Fabric runtime delegate shape for a shared failure scenario.
  - API/Type: `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricRuntimeGameTests.java:23-31` - existing delegate already present for the missing AE scenario.
  - Pattern: `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricGameTestEntrypoint.java:16-84` - current AE Fabric entrypoint methods and filter conventions.
  - External: `docs/layered-testing-ci.md:15-19` - nightly Fabric AE runtime currently uses the `ae_smoke` filter.

  **Acceptance Criteria**:
  - [ ] Fabric entrypoint contains a new `@GameTest` method for the binding-unavailable shared scenario.
  - [ ] The new entrypoint delegates through `MineAgentAeFabricRuntimeGameTests` and not directly to the shared scenario body.
  - [ ] `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke` exits `0` with the new Fabric-exposed scenario included in AE smoke coverage.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Fabric wrapper exposes the missing binding-unavailable scenario
    Tool: Grep
    Steps: Search `MineAgentAeFabricGameTestEntrypoint.java` for `boundTerminalApprovalFailsWhenAeBindingUnavailable` and confirm it calls into `MineAgentAeFabricRuntimeGameTests`.
    Expected: One Fabric `@GameTest` entrypoint exists and delegates through the runtime helper.
    Evidence: .sisyphus/evidence/task-2-fabric-binding-exposure.md

  Scenario: Fabric AE smoke lane still passes with the new scenario
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke`
    Expected: Exit code `0`; AE smoke runtime succeeds with the new scenario included.
    Evidence: .sisyphus/evidence/task-2-fabric-binding-smoke.log
  ```

  **Commit**: NO | Message: `test(ext-ae): align shared gametest parity across loaders` | Files: `ext-ae/fabric-1.20.1/src/main/java/**`

- [x] 3. Expose the orphaned binding-unavailable shared scenario through Forge

  **What to do**: Add the missing thin Forge wrapper for `AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)`, register it in `MineAgentAeGameTestBootstrap`, and keep the wrapper shape identical to the existing AE Forge GameTests: `GameTestRuntimeLease.runWhenAvailable(...)` plus a tiny `createPlayer(...)` helper only. Reuse the existing `mineagentae` namespace/batch conventions.
  **Must NOT do**: Do not move scenario logic into the Forge wrapper. Do not add Forge-only business assertions. Do not create a second bootstrap or a custom namespace.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: narrow Forge wrapper/bootstrap alignment.
  - Skills: `[]`
  - Omitted: `["git-master"]` - this is not a git task.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 4, 5, 14 | Blocked By: 1

  **References**:
  - Pattern: `base/forge-1.20.1/src/main/java/space/controlnet/mineagent/forge/gametest/ProposalBindingUnavailableGameTest.java:24-36` - base Forge wrapper for the same architectural pattern.
  - Pattern: `base/forge-1.20.1/src/main/java/space/controlnet/mineagent/forge/gametest/MineAgentGameTestBootstrap.java:12-35` - bootstrap list registration pattern.
  - Pattern: `ext-ae/forge-1.20.1/src/main/java/space/controlnet/mineagent/ae/forge/gametest/AeBoundTerminalApprovalSuccessGameTest.java:21-33` - existing AE Forge wrapper shape.
  - API/Type: `ext-ae/forge-1.20.1/src/main/java/space/controlnet/mineagent/ae/forge/gametest/MineAgentAeGameTestBootstrap.java:10-28` - current AE bootstrap registration list.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:51-128` - shared scenario body to expose.

  **Acceptance Criteria**:
  - [ ] A new Forge wrapper class exists for the binding-unavailable shared scenario.
  - [ ] `MineAgentAeGameTestBootstrap` registers the new wrapper class.
  - [ ] `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace` exits `0` with the new Forge-exposed scenario included.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Forge wrapper and bootstrap registration exist
    Tool: Grep
    Steps: Search `ext-ae/forge-1.20.1/src/main/java/.../gametest` for the new binding-unavailable wrapper class and confirm `MineAgentAeGameTestBootstrap` includes it.
    Expected: Exactly one new wrapper is registered in the bootstrap list.
    Evidence: .sisyphus/evidence/task-3-forge-binding-exposure.md

  Scenario: Forge AE GameTest server passes with the new wrapper
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace`
    Expected: Exit code `0`; Forge AE runtime succeeds with the new scenario included.
    Evidence: .sisyphus/evidence/task-3-forge-binding-smoke.log
  ```

  **Commit**: NO | Message: `test(ext-ae): align shared gametest parity across loaders` | Files: `ext-ae/forge-1.20.1/src/main/java/**`

- [x] 4. Add a loader-parity guard so shared AE scenarios cannot drift orphaned again

  **What to do**: Add the narrowest possible automated guard that proves the in-scope shared AE scenario inventory is exposed by both loaders. Prefer a test/support check built from existing source-of-truth lists or loader wrapper inventories rather than a generic new framework. The goal is to fail fast when a runnable shared scenario exists in common but is missing from Fabric entrypoint or Forge bootstrap/wrappers.
  **Must NOT do**: Do not introduce a heavyweight annotation processor, generic reflection framework, or repo-wide rule engine. Do not require manual parity inspection as the only safeguard.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: the task crosses common, Fabric, and Forge surfaces and must stay minimal without becoming tooling sprawl.
  - Skills: `[]`
  - Omitted: `["skill-creator"]` - no new skill should be invented for this.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 5, 14 | Blocked By: 1, 2, 3

  **References**:
  - Pattern: `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/GameTestPlayerFactory.java:1-10` - base keeps shared GameTest infrastructure minimal and explicit.
  - Pattern: `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricGameTestEntrypoint.java:16-84` - Fabric exposure surface to validate.
  - Pattern: `ext-ae/forge-1.20.1/src/main/java/space/controlnet/mineagent/ae/forge/gametest/MineAgentAeGameTestBootstrap.java:10-28` - Forge exposure surface to validate.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:51-174` - scenario surface under parity protection.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:44-320` - second shared scenario surface under parity protection.

  **Acceptance Criteria**:
  - [ ] An automated parity guard exists and fails if a shared AE runnable scenario is missing from either loader surface.
  - [ ] The parity guard passes once the Fabric and Forge binding-unavailable exposure work is complete.
  - [ ] The guard remains narrowly scoped to ext-ae and does not create a generic new test framework.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Parity guard passes on the aligned AE GameTest surface
    Tool: Bash
    Steps: Run the narrowest targeted test command that executes the new parity guard class in the module where it is implemented.
    Expected: Exit code `0`; the guard confirms every in-scope shared AE scenario is loader-visible.
    Evidence: .sisyphus/evidence/task-4-ae-parity-guard.log

  Scenario: Parity guard would fail on a missing loader exposure
    Tool: Grep
    Steps: Review the implemented guard and verify it explicitly checks both the Fabric entry surface and the Forge bootstrap/wrapper surface rather than one side only.
    Expected: The guard’s assertions name both loader exposure paths and would fail on future orphaning.
    Evidence: .sisyphus/evidence/task-4-ae-parity-guard-review.md
  ```

  **Commit**: NO | Message: `test(ext-ae): align shared gametest parity across loaders` | Files: `ext-ae/**/src/test/**`, `ext-ae/**/src/main/java/**`

- [x] 5. Normalize AE GameTest naming, batching, and nightly parity expectations

  **What to do**: Normalize the final AE GameTest surface so Fabric and Forge expose the same in-scope shared scenarios under stable naming and batch conventions. Keep Fabric within the `ae_smoke` nightly shape, keep Forge under `mineagentae`, and ensure any parity/report expectations in checked-in evidence or scripts remain consistent with the expanded shared scenario set. This task closes the architectural wave by making the parity shape stable for CI rollout.
  **Must NOT do**: Do not invent a new AE heavy lane. Do not rename existing working scenarios unnecessarily. Do not move scenario bodies out of common code.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: focused alignment across existing names and batch/filter conventions.
  - Skills: `[]`
  - Omitted: `["writing"]` - docs update belongs in Task 15.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 14, 15 | Blocked By: 1, 2, 3, 4

  **References**:
  - Pattern: `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricGameTestEntrypoint.java:16-84` - current `ae_smoke_*` batch shape on Fabric.
  - Pattern: `ext-ae/forge-1.20.1/src/main/java/space/controlnet/mineagent/ae/forge/gametest/MineAgentAeGameTestBootstrap.java:12-18` - current Forge AE wrapper set.
  - External: `ci-reports/parity/gametest-parity-report.md:25-29` - current Fabric AE parity inventory.
  - External: `ci-reports/parity/gametest-parity-report.md:66-70` - current Forge AE parity inventory.
  - External: `docs/layered-testing-ci.md:15-19` - current nightly Fabric AE smoke lane description.

  **Acceptance Criteria**:
  - [ ] Fabric and Forge expose the same in-scope shared AE scenario set after Wave 1.
  - [ ] Fabric keeps the nightly-compatible `ae_smoke` filter shape; Forge keeps the `mineagentae` namespace/batch shape.
  - [ ] Existing parity/report expectations remain consistent with the expanded AE shared scenario surface.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Fabric and Forge inventories describe the same in-scope AE scenarios
    Tool: Grep
    Steps: Compare the AE Fabric entrypoint methods and the Forge bootstrap wrapper list against the Wave 1 parity inventory.
    Expected: No in-scope shared scenario is present on only one loader.
    Evidence: .sisyphus/evidence/task-5-ae-loader-parity.md

  Scenario: Nightly-compatible AE smoke naming survives normalization
    Tool: Grep
    Steps: Search Fabric AE entrypoint batch names and confirm they still match `ae_smoke*` conventions used by nightly filtering.
    Expected: AE nightly filter compatibility is preserved.
    Evidence: .sisyphus/evidence/task-5-ae-smoke-naming.md
  ```

  **Commit**: YES | Message: `test(ext-ae): align shared gametest parity across loaders` | Files: `ext-ae/common-1.20.1/**`, `ext-ae/fabric-1.20.1/**`, `ext-ae/forge-1.20.1/**`

- [x] 6. Add deterministic list and pagination regression tests for `AiTerminalPartOperations`

  **What to do**: Add a new AE common regression class focused on the read-only list APIs in `AiTerminalPartOperations`: `listItems(...)` and `listCraftables(...)`. Lock down query filtering, sorted output order, `craftableOnly`, limit clamping, null/blank page token handling, and next-page token behavior. Build the tests around stable observable outputs rather than private implementation details.
  **Must NOT do**: Do not convert this into runtime/GameTests. Do not test internal stream ordering mechanics directly. Do not rewrite `AiTerminalPartOperations` unless a tiny seam is strictly required for deterministic testing.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this is the highest-value deterministic business-logic coverage gap and needs careful boundary selection.
  - Skills: `[]`
  - Omitted: `["playwright", "github-cli"]`

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 7, 11, 13 | Blocked By: none

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/AiTerminalPartOperations.java:39-90` - list and craftables behavior to lock down.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/tools/AeToolProviderRenderRegressionTest.java:77-105` - existing fake terminal style and stable payload assertions.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/tools/AeThreadConfinementRegressionTest.java:112-147` - existing deterministic invocation recording pattern.
  - API/Type: `ext-ae/core/src/main/java/space/controlnet/mineagent/ae/core/terminal/AiTerminalData.java` - list result/value shapes already used in current regressions.

  **Acceptance Criteria**:
  - [ ] A targeted regression class exists for `AiTerminalPartOperations` list/pagination behavior.
  - [ ] The targeted class proves sorted results, next-page token stability, and limit clamp behavior.
  - [ ] `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.part.AiTerminalPartOperationsListRegressionTest'` exits `0`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: List and craftables regression class passes
    Tool: Bash
    Steps: Run `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.part.AiTerminalPartOperationsListRegressionTest'`
    Expected: Exit code `0`; all list/pagination assertions pass deterministically.
    Evidence: .sisyphus/evidence/task-6-part-ops-list.log

  Scenario: Page-token and limit edge cases are explicitly asserted
    Tool: Read
    Steps: Inspect the new regression class and confirm it contains tests for blank/null page token, invalid token handling, and limit clamp behavior.
    Expected: Edge-path coverage exists in the targeted regression class.
    Evidence: .sisyphus/evidence/task-6-part-ops-list-review.md
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/test/java/**`, optional minimal seam in `ext-ae/common-1.20.1/src/main/java/**`

- [x] 7. Add job lifecycle, CPU selection, and cancel-state regression tests for `AiTerminalPartOperations`

  **What to do**: Add a second targeted regression class for the mutation/stateful parts of `AiTerminalPartOperations`: `resolveKey(...)`, `selectCpu(...)`, `jobStatus(...)`, `cancelJob(...)`, `jobStateChange(...)`, `clearJobs()`, and `refreshJob(...)` through observable behavior. Include success and failure-path coverage such as missing item IDs, unavailable CPU selection, unknown job IDs, canceled jobs, and done jobs.
  **Must NOT do**: Do not assert private record internals or incidental map state. Do not replace runtime GameTests with unit tests for loader-visible lifecycle behavior.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this task protects the most mutation-heavy AE logic with deterministic regressions.
  - Skills: `[]`
  - Omitted: `["review-work"]` - review belongs to the final verification wave.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 11, 13 | Blocked By: 6

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/AiTerminalPartOperations.java:92-315` - simulation/request/job lifecycle and helper behaviors.
  - Test: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:124-250` - current runtime expectations for clear-jobs and CPU-unavailable behavior.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/tools/AeThreadConfinementRegressionTest.java:113-147` - deterministic invocation recording pattern to mimic.
  - API/Type: `ext-ae/core/src/main/java/space/controlnet/mineagent/ae/core/terminal/AiTerminalData.java` - `AeCraftRequest` and `AeJobStatus` observable contracts.

  **Acceptance Criteria**:
  - [ ] A targeted regression class exists for `AiTerminalPartOperations` mutation/state behavior.
  - [ ] Unknown job IDs, unavailable CPU selection, and cancel/done state transitions are explicitly asserted.
  - [ ] `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.part.AiTerminalPartOperationsJobLifecycleRegressionTest'` exits `0`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Job lifecycle regression class passes
    Tool: Bash
    Steps: Run `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.part.AiTerminalPartOperationsJobLifecycleRegressionTest'`
    Expected: Exit code `0`; job lifecycle boundaries are regression-protected.
    Evidence: .sisyphus/evidence/task-7-part-ops-job.log

  Scenario: Failure paths are stable and explicit
    Tool: Read
    Steps: Inspect the new regression class and confirm it contains explicit assertions for unknown job, unavailable CPU, and canceled/done transitions.
    Expected: Negative-path coverage is present and message/status assertions are deterministic.
    Evidence: .sisyphus/evidence/task-7-part-ops-job-review.md
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/test/java/**`, optional minimal seam in `ext-ae/common-1.20.1/src/main/java/**`

- [x] 8. Add `AeTerminalContextResolver` success and invalidation regression coverage

  **What to do**: Add a focused common regression class for `AeTerminalContextResolver` that covers both successful and failure/invalidation paths. Explicitly test `fromPlayer(...)` success when the player is in an AE terminal menu with a live host, plus failures for null player, wrong menu type, missing host, wrong host type, and removed host. Explicitly test `fromPlayerAtBinding(...)` success, wrong-side lookup failure, invalid dimension, missing block entity, and removed host.
  **Must NOT do**: Do not rely only on existing GameTests for resolver coverage. Do not couple tests to implementation trivia beyond observable `Optional` resolution behavior.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: medium-complexity integration-flavored unit coverage across menus, hosts, and bindings.
  - Skills: `[]`
  - Omitted: `["playwright"]`

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 12 | Blocked By: none

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/terminal/AeTerminalContextResolver.java:21-146` - success and invalidation logic to cover.
  - Test: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:130-279` - runtime expectations for binding-based approval success handoff.
  - Test: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:51-128` - runtime expectations for binding invalidation failure.
  - Pattern: `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/ProposalBindingUnavailableGameTestScenarios.java:58-192` - base binding-resolution failure expectations.

  **Acceptance Criteria**:
  - [ ] A targeted resolver regression class exists under `ext-ae/common-1.20.1/src/test/java`.
  - [ ] The class includes both `fromPlayer(...)` and `fromPlayerAtBinding(...)` success and failure-path assertions.
  - [ ] `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.terminal.AeTerminalContextResolverRegressionTest'` exits `0`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Resolver regression class passes
    Tool: Bash
    Steps: Run `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.terminal.AeTerminalContextResolverRegressionTest'`
    Expected: Exit code `0`; resolver success and invalidation paths are covered.
    Evidence: .sisyphus/evidence/task-8-resolver.log

  Scenario: Binding and removed-host edge paths are present
    Tool: Read
    Steps: Inspect the new resolver regression class and confirm dedicated assertions for removed-host, wrong-side, and invalid-dimension failure cases.
    Expected: All named edge paths are asserted explicitly.
    Evidence: .sisyphus/evidence/task-8-resolver-review.md
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/test/java/**`

- [x] 9. Add richer formatting and fallback regression coverage for `AeToolOutputRenderer`

  **What to do**: Expand renderer regression coverage from shape detection into real formatting behavior. Add assertions for item-list rendering, missing-items rendering, pagination lines, truncation/max-entry behavior, empty-result handling, and malformed payload fallbacks. Keep the tests at the renderer-contract layer; they should verify what the player-visible line output looks like, not internal branching.
  **Must NOT do**: Do not convert renderer tests into client runtime tests. Do not depend on Minecraft client rendering; stay in pure JUnit.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: renderer coverage needs comprehensive contract assertions without leaking into UI runtime.
  - Skills: `[]`
  - Omitted: `["frontend-claude", "playwright"]` - this is not visual UI automation.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 13 | Blocked By: none

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/client/AeToolOutputRenderer.java` - renderer implementation to contract-lock.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/client/AeToolOutputRendererRegressionTest.java:10-63` - current shallow renderer coverage to extend rather than replace.
  - API/Type: `ext-ae/core/src/main/java/space/controlnet/mineagent/ae/core/terminal/AiTerminalData.java` - list/job payload shapes that drive output formatting.

  **Acceptance Criteria**:
  - [ ] Renderer regression coverage includes list formatting, missing-items formatting, pagination, truncation, and malformed fallback behavior.
  - [ ] Existing renderer regression class or a new focused companion class remains pure JUnit and deterministic.
  - [ ] `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.client.AeToolOutputRenderer*'` exits `0`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Renderer regression suite passes
    Tool: Bash
    Steps: Run `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.client.AeToolOutputRenderer*'`
    Expected: Exit code `0`; renderer contract assertions are green.
    Evidence: .sisyphus/evidence/task-9-renderer.log

  Scenario: Renderer truncation and malformed fallback paths are asserted
    Tool: Read
    Steps: Inspect the renderer regression file(s) for explicit assertions on truncation/max-entry behavior and malformed payload fallback.
    Expected: Both positive formatting and negative fallback paths are covered.
    Evidence: .sisyphus/evidence/task-9-renderer-review.md
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/test/java/**`

- [x] 10. Add AE init and registration wiring regression coverage

  **What to do**: Add a narrow regression test that locks down AE registration wiring in `MineAgentAe.init()`: provider registration under `ae`, group-id assignment to `mineagentae`, terminal context resolver registration, renderer registration, and part registry initialization side effects that can be asserted safely. Keep the scope to observable registration contracts; do not turn this into a broad bootstrap refactor.
  **Must NOT do**: Do not test log lines. Do not rely on global mutable registry state without cleanup. Do not refactor init flow unless a small test seam is required.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: small but valuable regression around wiring drift.
  - Skills: `[]`
  - Omitted: `["secret-guard"]`

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 15 | Blocked By: none

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/MineAgentAe.java:20-27` - AE init wiring to freeze.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/MineAgentAePartRegistries.java` - part registry side effects to assert as narrowly as possible.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/tools/AeToolProviderMetadataRenderRegressionTest.java:9-31` - existing metadata assertions that complement registry wiring.

  **Acceptance Criteria**:
  - [ ] A narrow wiring regression exists and asserts provider, group-id, resolver, and renderer registration contracts.
  - [ ] Registry state is cleaned up or isolated so the test remains deterministic.
  - [ ] `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.MineAgentAeRegistrationRegressionTest'` exits `0`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: AE registration regression class passes
    Tool: Bash
    Steps: Run `./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test --tests 'space.controlnet.mineagent.ae.common.MineAgentAeRegistrationRegressionTest'`
    Expected: Exit code `0`; AE init wiring contracts are stable.
    Evidence: .sisyphus/evidence/task-10-ae-registration.log

  Scenario: Wiring regression stays narrow and observable
    Tool: Read
    Steps: Inspect the new regression class and confirm it asserts registry outcomes rather than logs or unrelated bootstrap details.
    Expected: Test scope is limited to observable wiring contracts.
    Evidence: .sisyphus/evidence/task-10-ae-registration-review.md
  ```

  **Commit**: YES | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/test/java/**`, optional minimal seam in `ext-ae/common-1.20.1/src/main/java/**`

- [x] 11. Add shared runtime coverage for AE terminal removal and menu invalidation lifecycle

  **What to do**: Add one new shared AE runtime/GameTest scenario in common code that proves terminal removal or host invalidation produces the correct observable lifecycle outcome: active jobs are cleared, menu/context liveness is invalidated, and post-removal queries fail in the intended way. Expose it through both Fabric and Forge using the Wave 1 parity contract. This task is the runtime complement to the new `AiTerminalPartOperations` regressions and must use the real loader-visible GameTest surfaces rather than only fakes.
  **Must NOT do**: Do not add loader-specific scenario bodies. Do not replace existing lifecycle scenarios; extend the shared suite narrowly. Do not broaden into unrelated AE2 world simulation.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: runtime lifecycle assertions must stay shared, deterministic, and loader-visible.
  - Skills: `[]`
  - Omitted: `["playwright"]`

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 14, 15 | Blocked By: 1, 6, 7

  **References**:
  - Pattern: `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/SessionLifecycleGameTestScenarios.java` - base runtime lifecycle logic belongs in shared common scenarios.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:124-193` - current terminal teardown coverage to extend rather than duplicate.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/AiTerminalPart.java` - real part lifecycle surface this GameTest must protect.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/terminal/AeTerminalContextResolver.java:23-79` - runtime-visible menu/binding invalidation behavior linked to removal.

  **Acceptance Criteria**:
  - [ ] A new shared AE runtime scenario exists in common code for terminal removal/menu invalidation lifecycle.
  - [ ] Both Fabric and Forge expose the scenario through thin wrappers only.
  - [ ] Fabric AE smoke and Forge AE GameTests both pass with the new lifecycle scenario included.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Shared terminal-removal lifecycle scenario passes on Fabric
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke`
    Expected: Exit code `0`; the new lifecycle scenario is loader-visible on Fabric and passes.
    Evidence: .sisyphus/evidence/task-11-fabric-terminal-removal.log

  Scenario: Shared terminal-removal lifecycle scenario passes on Forge
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace`
    Expected: Exit code `0`; the same shared lifecycle scenario is loader-visible on Forge and passes.
    Evidence: .sisyphus/evidence/task-11-forge-terminal-removal.log
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/main/java/**`, `ext-ae/fabric-1.20.1/src/main/java/**`, `ext-ae/forge-1.20.1/src/main/java/**`

- [x] 12. Add shared runtime coverage for binding-based re-resolution success and stale-binding failure

  **What to do**: Add one new shared AE runtime/GameTest scenario that proves binding-based context re-resolution works when the host is still valid and fails cleanly when the binding becomes stale. This task should cover the positive gap not directly locked down today: successful re-resolution from binding through the real loader-visible GameTest path, not just failure after invalidation. Expose it through both loaders using the existing thin-wrapper pattern.
  **Must NOT do**: Do not copy resolver logic into loaders. Do not overlap this scenario with the existing approval-success handoff unless the new assertions are strictly about re-resolution success vs stale binding.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this closes the most important runtime success-path gap around binding and context continuity.
  - Skills: `[]`
  - Omitted: `["github-cli"]`

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 14, 15 | Blocked By: 1, 8

  **References**:
  - Pattern: `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/ProposalBindingUnavailableGameTestScenarios.java:35-174` - base pattern for binding success/failure assertions around approval flow.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java:130-279` - existing approval success handoff scenario to extend carefully.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/terminal/AeTerminalContextResolver.java:43-106` - binding-based resolver logic to prove at runtime.
  - Pattern: `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricRuntimeGameTests.java:33-54` - current Fabric delegate shape for binding-related AE runtime scenarios.

  **Acceptance Criteria**:
  - [ ] A new shared AE runtime scenario exists for binding-based re-resolution success and stale-binding failure.
  - [ ] Both loaders expose the scenario through thin wrappers only.
  - [ ] Fabric AE smoke and Forge AE GameTests both pass with the new binding re-resolution coverage included.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Binding re-resolution shared scenario passes on Fabric
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke`
    Expected: Exit code `0`; Fabric executes the shared binding re-resolution scenario successfully.
    Evidence: .sisyphus/evidence/task-12-fabric-binding-reresolution.log

  Scenario: Binding re-resolution shared scenario passes on Forge
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace`
    Expected: Exit code `0`; Forge executes the same shared binding re-resolution scenario successfully.
    Evidence: .sisyphus/evidence/task-12-forge-binding-reresolution.log
  ```

  **Commit**: NO | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/main/java/**`, `ext-ae/fabric-1.20.1/src/main/java/**`, `ext-ae/forge-1.20.1/src/main/java/**`

- [x] 13. Add shared runtime coverage for request/simulate/status/cancel edge cases through loader-visible AE flows

  **What to do**: Add a shared AE runtime/GameTest scenario that covers one coherent end-to-end mutation edge family that current smoke coverage does not fully lock down: request/simulate/status/cancel with observable edge outcomes such as unknown job after clear/cancel, terminal-local isolation, or stable failure semantics after lifecycle disruption. Keep the assertions on externally visible AE terminal behavior rather than internal implementation sequencing.
  **Must NOT do**: Do not create three separate scenario bodies if one coherent shared scenario can cover the edge family. Do not duplicate assertions already fully protected by existing smoke scenarios.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: this is a bounded runtime expansion with several possible edge-path assertions that must stay disciplined.
  - Skills: `[]`
  - Omitted: `["artistry"]` - this should stay conventional and pattern-following.

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 14, 15 | Blocked By: 1, 6, 7, 9

  **References**:
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:44-117` - existing request lifecycle and terminal-isolation shape.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java:258-320` - current failure/recovery branch area to extend carefully.
  - API/Type: `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/AiTerminalPartOperations.java:120-229` - runtime-visible request/status/cancel behaviors to assert.
  - Test: `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/tools/AeToolProviderRenderRegressionTest.java:77-105` - existing contract expectations for request/status/cancel payloads.

  **Acceptance Criteria**:
  - [ ] A new shared runtime scenario exists for the chosen request/simulate/status/cancel edge family.
  - [ ] Both Fabric and Forge expose the scenario without duplicating its business logic.
  - [ ] Fabric AE smoke and Forge AE GameTests both pass with the new edge-path scenario included.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Request/status/cancel edge scenario passes on Fabric
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke`
    Expected: Exit code `0`; the new shared edge-path scenario passes on Fabric.
    Evidence: .sisyphus/evidence/task-13-fabric-request-edge.log

  Scenario: Request/status/cancel edge scenario passes on Forge
    Tool: Bash
    Steps: Run `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace`
    Expected: Exit code `0`; the same shared edge-path scenario passes on Forge.
    Evidence: .sisyphus/evidence/task-13-forge-request-edge.log
  ```

  **Commit**: YES | Message: `test(ext-ae): harden terminal regression coverage` | Files: `ext-ae/common-1.20.1/src/main/java/**`, `ext-ae/fabric-1.20.1/src/main/java/**`, `ext-ae/forge-1.20.1/src/main/java/**`

- [x] 14. Add AE Forge GameTests to the nightly CI lane with progressive rollout only

  **What to do**: Update `.github/workflows/layered-testing.yml` so AE Forge GameTests run in the nightly lane only. Keep PR/dev JUnit behavior unchanged. Capture Forge AE runtime logs/artifacts alongside the existing nightly outputs and integrate the new step into the lane summary/report expectations. This task is intentionally limited to nightly-first rollout; do not promote AE Forge runtime coverage to PR or dev in this plan.
  **Must NOT do**: Do not add PR gating. Do not change release workflow behavior. Do not rewrite unrelated CI structure or base-mod lanes.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: focused workflow wiring change with a clear rollout rule.
  - Skills: `[]`
  - Omitted: `["github-cli"]` - no remote GitHub interaction is required.

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: F1, F3, F4 | Blocked By: 2, 3, 4, 5, 11, 12, 13

  **References**:
  - External: `.github/workflows/layered-testing.yml:138-206` - nightly lane structure to extend.
  - External: `.github/workflows/layered-testing.yml:76-137` - dev lane scope to preserve unchanged for this plan.
  - Pattern: `AGENTS.md:68-74` - canonical AE Forge and Fabric GameTest commands.
  - External: `docs/layered-testing-ci.md:13-19` - documented lane matrix to keep aligned.
  - External: `REPO.md:919-921` - current CI lane descriptions to update consistently in Task 15.

  **Acceptance Criteria**:
  - [ ] Nightly workflow contains `timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace` or an equivalent quoted command.
  - [ ] PR/dev lanes remain free of AE Forge GameTest gating in this plan.
  - [ ] Nightly artifact collection captures AE Forge runtime evidence/log output alongside existing reports.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Nightly workflow contains AE Forge GameTest rollout and preserves lane boundaries
    Tool: Read
    Steps: Inspect `.github/workflows/layered-testing.yml` and confirm AE Forge GameTests appear only in nightly, not PR/dev.
    Expected: Nightly includes AE Forge runtime; PR/dev do not gain AE Forge gating.
    Evidence: .sisyphus/evidence/task-14-nightly-forge-wiring.md

  Scenario: Nightly workflow still captures AE runtime artifacts
    Tool: Read
    Steps: Inspect the nightly artifact upload block and confirm AE Forge runtime logs/reports are included.
    Expected: Artifact collection covers the new AE Forge runtime step.
    Evidence: .sisyphus/evidence/task-14-nightly-forge-artifacts.md
  ```

  **Commit**: NO | Message: `ci(ext-ae): add forge gametest nightly coverage` | Files: `.github/workflows/layered-testing.yml`

- [x] 15. Synchronize runtime testing docs and rollout policy after the AE hardening changes

  **What to do**: Update `REPO.md` and `docs/layered-testing-ci.md` so the documented AE test architecture, nightly lane behavior, and canonical verification commands match the final implementation. Record the progressive rollout policy explicitly: AE Forge GameTests are introduced in nightly first, PR/dev remain unchanged, and promotion beyond nightly is deferred until stability is proven. If checked-in parity evidence or report docs need updating to stay accurate, do that here as part of the same task.
  **Must NOT do**: Do not change project rules in `AGENTS.md` unless a command reference is genuinely outdated and repo guidance requires it. Do not document future PR/dev promotion as already completed.

  **Recommended Agent Profile**:
  - Category: `writing` - Reason: this is primarily doc synchronization with some CI policy wording.
  - Skills: `[]`
  - Omitted: `["frontend-claude"]`

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: F1, F2, F4 | Blocked By: 5, 10, 11, 12, 13, 14

  **References**:
  - External: `REPO.md:844-925` - current AE runtime/testing and lane policy section to update.
  - External: `docs/layered-testing-ci.md:13-19` - lane matrix that must reflect nightly AE Forge rollout.
  - Pattern: `AGENTS.md:68-74` - canonical GameTest command references to preserve.
  - External: `ci-reports/parity/gametest-parity-report.md:25-29,66-70` - checked-in parity evidence that may need alignment with the final shared AE scenario surface.

  **Acceptance Criteria**:
  - [ ] `REPO.md` documents the final AE GameTest architecture and nightly AE Forge rollout accurately.
  - [ ] `docs/layered-testing-ci.md` documents the same nightly rollout and canonical commands accurately.
  - [ ] Any checked-in parity/report documentation touched by the implementation reflects the final in-scope AE shared scenario set.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: REPO.md and CI docs match the final AE runtime lane behavior
    Tool: Read
    Steps: Inspect `REPO.md` and `docs/layered-testing-ci.md` after the changes and compare them to `.github/workflows/layered-testing.yml`.
    Expected: Docs and workflow describe the same AE nightly rollout and commands.
    Evidence: .sisyphus/evidence/task-15-docs-sync.md

  Scenario: Rollout policy is documented as nightly-first only
    Tool: Grep
    Steps: Search updated docs for the AE Forge runtime command and confirm PR/dev promotion is not documented as completed work.
    Expected: Docs describe nightly-first rollout accurately and conservatively.
    Evidence: .sisyphus/evidence/task-15-rollout-policy.md
  ```

  **Commit**: YES | Message: `ci(ext-ae): add forge gametest nightly coverage` | Files: `REPO.md`, `docs/layered-testing-ci.md`, optional parity-report docs/evidence paths

## Final Verification Wave (MANDATORY — after ALL implementation tasks)
> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**
> **Never mark F1-F4 as checked before getting user's okay.** Rejection or user feedback -> fix -> re-run -> present again -> wait for okay.
- [x] F1. Plan Compliance Audit — oracle
- [x] F2. Code Quality Review — unspecified-high
- [x] F3. Real Manual QA — unspecified-high (+ playwright if UI)
- [x] F4. Scope Fidelity Check — deep

## Commit Strategy
- Commit A after Wave 1: `test(ext-ae): align shared gametest parity across loaders`
- Commit B after Wave 2 and runtime additions that depend only on test code: `test(ext-ae): harden terminal regression coverage`
- Commit C after Wave 3 CI/docs rollout: `ci(ext-ae): add forge gametest nightly coverage`
- Do not amend or squash within the plan unless the executor explicitly chooses to after passing verification.

## Success Criteria
- All in-scope shared AE GameTest scenarios are loader-visible through both Fabric and Forge, or are explicitly marked out-of-scope by this plan.
- No new loader-specific scenario logic exists outside `ext-ae/common-1.20.1/.../gametest/*Scenarios.java`.
- The new deterministic regression suites cover the named weak points with stable success and failure-path assertions.
- AE Forge runtime coverage is visible in nightly CI and documented without disturbing PR/dev fast feedback lanes.
