# AE hardening test coverage review

Date: 2026-06-16

## Verdict

The current AE hardening implementation is broadly covered by tests, with one important qualification: common-module logic and loader exposure are covered well, Fabric runtime coverage is represented by shared GameTest wrappers, but Forge runtime execution evidence is inconsistent/stale and should be refreshed before committing the full AE hardening work.

Fresh command run during this review:

```bash
./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test
```

Result: `BUILD SUCCESSFUL`.

## Covered implementation areas

### `AiTerminalPartOperations` list and pagination

Covered by:
- `AiTerminalPartOperationsListRegressionTest`
- `AiTerminalPartOperationsListPagination`

Assertions cover:
- case-insensitive trimmed query filtering
- stable item-id sorting
- `craftableOnly` filtered pagination
- null, blank, invalid, and negative page-token fallback
- lower and upper limit clamp behavior
- next-page token generation after filtering

Coverage limitation:
- The test locks the pure-Java pagination seam. It does not directly exercise AE2 inventory/craftable extraction from a real `IGrid` in plain JUnit. Runtime scenarios cover broader AE operation flows, but not a dedicated real-grid list pagination path.

### `AiTerminalPartOperations` job lifecycle

Covered by:
- `AiTerminalPartOperationsJobLifecycleRegressionTest`
- AE shared runtime GameTests in `AeCraftLifecycleIsolationGameTestScenarios`

Assertions cover:
- known/blank/invalid/unknown item id resolution
- case-insensitive CPU substring selection and unavailable CPU matching
- unknown job status/cancel output
- calculating, submitted, done, canceled, and failed status transitions
- missing-item error message stability
- `jobStateChange`
- `cancelJob`
- `clearJobs`
- requested live-job set clearing
- terminal-local cancel/clear isolation through runtime GameTest scenario

### `AeTerminalContextResolver`

Covered by:
- `AeTerminalContextResolverRegressionTest`
- shared runtime scenarios in `AeBindingFailureGameTestScenarios`
- shared runtime scenario `terminalRemovalInvalidatesMenuContextAndClearsJobs`

Assertions cover:
- live AE terminal menu resolves
- null/wrong menu/missing host/wrong host/removed host returns empty
- binding lookup success
- wrong-side binding returns empty
- invalid dimension returns empty
- missing block entity returns empty
- removed host returns empty
- binding-based context re-resolution succeeds until the binding becomes stale in runtime scenario

Coverage limitation:
- Plain JUnit covers resolver seam and adapter-independent behavior. Real Minecraft/AE adapter wiring is covered through shared GameTest scenarios rather than common JUnit.

### `AeToolOutputRenderer`

Covered by:
- `AeToolOutputRendererRegressionTest`
- `AeToolOutputRendererFallbackRegressionTest`

Assertions cover:
- `canRender` rejects unknown, non-AE, and ambiguous bare empty result shapes
- explicit empty AE list renders as "No items found."
- AE list line formatting, craftable flag, pagination, error line
- list truncation after eight visible entries
- job status formatting with missing items, truncation, and error line
- malformed/unsupported payload stability
- registry fallback behavior after malformed AE payloads

### AE init and registration wiring

Covered by:
- `MineAgentAeRegistrationRegressionTest`

Assertions cover:
- `initCommonWiring()` registers AE provider under `ae`
- group id resolves to `mineagentae`
- tool specs are visible
- AE terminal resolver is registered
- AE renderer renders job status
- source-level check that `MineAgentAe.init()` still delegates to `MineAgentAePartRegistries.init()`
- source-level check that part/model registration calls remain present

Coverage limitation:
- Part-registry bootstrap itself is guarded by source checks, not runtime loading, because plain common JUnit cannot load AE2/Minecraft part registry state safely.

### Shared AE GameTest parity

Covered by:
- `AeSharedGameTestLoaderParityGuardTest`
- Fabric entrypoint/runtime delegate source
- Forge bootstrap/wrapper source

Assertions cover a frozen nine-scenario shared AE surface:
- `craftLifecycleIsolation`
- `boundTerminalApprovalSuccessHandoff`
- `boundTerminalApprovalFailsWhenAeBindingUnavailable`
- `terminalTeardownClearsLiveJobs`
- `bindingInvalidationAfterTerminalRemovalOrWrongSide`
- `bindingBasedContextReresolutionSucceedsUntilBindingBecomesStale`
- `cpuTargetedUnavailableCpuBranch`
- `terminalRemovalInvalidatesMenuContextAndClearsJobs`
- `cancelAndClearStayTerminalLocalAfterSubmittedRequest`

The guard verifies:
- the common scenario inventory is exactly these nine runnable methods
- every scenario has a Fabric entrypoint and delegates through `MineAgentAeFabricRuntimeGameTests`
- every scenario has a Forge wrapper registered in `MineAgentAeGameTestBootstrap`
- every Forge wrapper calls the expected shared scenario body

## Verification evidence caveat

The common-module JUnit coverage is fresh and passed in this review.

Existing evidence files are mixed:
- Some older evidence records Forge runtime blocked by `InvalidModFileException ... version (main)` and `Failed to find system mod: minecraft`.
- Later evidence records those startup blocker signatures as resolved.
- `task-19-forge-gametest-summary.md` still records an overall blocker around non-interactive GameTest report emission/clean termination, while also noting a final ext-AE Forge run that reached `BUILD SUCCESSFUL` and reported `All 0 required tests passed :)`.

Before committing the full AE hardening work, rerun or refresh:

```bash
timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:fabric-1.20.1:runGametest --stacktrace -Dfabric-api.gametest.filter=ae_smoke
timeout 25m ./gradlew --no-daemon --configure-on-demand :ext-ae:forge-1.20.1:runGameTestServer --stacktrace
```

If Forge still emits no XML or exits only through timeout/process cleanup, treat that as a remaining CI/reporting issue, not a unit-test coverage gap.
