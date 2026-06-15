# Progress resume

Date: 2026-06-16

## Current branch state

- Branch: `dev`
- Local branch is ahead of `origin/dev` by local commits.
- Most recent local commits at this checkpoint:
  - `d83e8da docs: record project understanding`
  - `9ce92fd ci: add AE Forge runtime to nightly lane`

## Main previous workstream

The active unfinished-looking worktree is from the `ae-test-hardening` workstream.

The plan file `.omo/plans/ae-test-hardening.md` marks Tasks 1-15 and final verification F1-F4 as completed. The work hardens AE extension testing across:

- shared AE GameTest parity
- Fabric/Forge loader exposure for shared scenarios
- source-level parity guard
- `AiTerminalPartOperations` list/pagination and job lifecycle regressions
- `AeTerminalContextResolver` regressions
- `AeToolOutputRenderer` regressions
- AE init/registration wiring regression
- terminal removal, binding re-resolution, and cancel/clear runtime scenarios
- nightly-only AE Forge GameTest CI rollout
- docs and parity report synchronization

## Important implemented surfaces visible in the worktree

Modified tracked files include:
- `REPO.md`
- `docs/layered-testing-ci.md`
- `ci-reports/parity/gametest-parity-report.{json,md}`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/MineAgentAe.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/client/AeToolOutputRenderer.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/part/AiTerminalPartOperations.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/terminal/AeTerminalContextResolver.java`
- `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricGameTestEntrypoint.java`
- `ext-ae/fabric-1.20.1/src/main/java/space/controlnet/mineagent/ae/fabric/gametest/MineAgentAeFabricRuntimeGameTests.java`
- `ext-ae/forge-1.20.1/src/main/java/space/controlnet/mineagent/ae/forge/gametest/MineAgentAeGameTestBootstrap.java`

Untracked implementation/test files include:
- `AiTerminalPartOperationsJobLifecycle.java`
- `AiTerminalPartOperationsListPagination.java`
- `AeTerminalContextResolution.java`
- `MineAgentAeRegistrationRegressionTest.java`
- `AeToolOutputRendererFallbackRegressionTest.java`
- `AeSharedGameTestLoaderParityGuardTest.java`
- `AiTerminalPartOperationsJobLifecycleRegressionTest.java`
- `AiTerminalPartOperationsListRegressionTest.java`
- `AeTerminalContextResolverRegressionTest.java`
- new Forge AE GameTest wrappers for binding re-resolution, binding unavailable, cancel/clear isolation, and terminal removal invalidation

## Verification and blockers recorded

Useful evidence files:
- `.omo/evidence/task-12-full-verification-matrix.md`: records a full approved matrix as PASS in a prior rename/test-adoption context.
- `.omo/evidence/task-19-forge-gametest-summary.md`: records that historical Forge startup blocker signatures are gone, but non-interactive Forge GameTest report emission/clean termination remained BLOCKED in a bounded run.
- `.omo/evidence/f2-code-test-quality.md` and `.omo/evidence/f3-scenario-replay-validation.md`: record older Forge runtime blocker/Forge replay blocked verdicts.

Current interpretation:
- Implementation for AE test hardening appears complete according to the plan checklist and notes.
- The main unresolved risk is verification freshness/consistency around Forge GameTest behavior: one evidence note records final ext-AE Forge unblock PASS, while the same summary also records report/termination failures and an overall BLOCKED verdict for non-interactive report emission.
- Java LSP diagnostics repeatedly timed out during earlier AE work; Gradle targeted tests and GameTest runs were used as authoritative signals.

## Likely next step

Before committing the AE hardening work, rerun a fresh narrow verification set and reconcile the Forge GameTest status:

```bash
./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test
timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke
timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace
```

If Forge still reports `BUILD SUCCESSFUL` but lacks XML/clean non-interactive termination, decide whether the remaining work is a CI/reporting fix or an accepted documented runtime limitation before making the AE hardening commit.
