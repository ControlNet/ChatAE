## 2026-04-22T17:11:06Z Task: session-start
Initial note file for accumulated implementation learnings.

## 2026-04-22T17:17:31Z Task 1: AE shared scenario inventory
- The AE shared GameTest surface is finite today: `AeCraftLifecycleIsolationGameTestScenarios` contributes 3 public scenario methods and `AeBindingFailureGameTestScenarios` contributes 3 more, for a total of 6 runnable shared scenarios.
- Fabric already follows the base pattern for 5 of the 6 methods: thin runtime delegates in `MineAgentAeFabricRuntimeGameTests` and thin `@GameTest` entrypoints in `MineAgentAeFabricGameTestEntrypoint` using `ae_smoke_*` batches and filter matching.
- Forge also follows the base pattern for the same 5 methods: one thin wrapper class per scenario plus `MineAgentAeGameTestBootstrap` list registration under `mineagentae`.
- The known gap is not missing common logic; it is missing loader exposure for an already-runnable common scenario.

## 2026-04-23T00:00:00Z Task 2: Fabric binding-unavailable exposure
- Added the missing Fabric AE smoke entrypoint for `boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` in `MineAgentAeFabricGameTestEntrypoint`.
- The new method uses the `ae_smoke_binding_unavailable` batch and keeps the existing filter/runtime-lease pattern intact.

## 2026-04-23T00:00:00Z Task 3: Forge binding-unavailable exposure
- Added the missing Forge AE wrapper for `boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` in `AeBoundTerminalApprovalBindingUnavailableGameTest`.
- The wrapper mirrors the existing AE Forge success test shape: `GameTestRuntimeLease.runWhenAvailable(...)` plus a tiny local `createPlayer(...)` helper and `mineagentae` registration.

## 2026-04-23T00:00:00Z Task 4: AE loader parity guard
- Added `ext-ae/common-1.20.1/src/test/java/space/controlnet/mineagent/ae/common/gametest/AeSharedGameTestLoaderParityGuardTest.java` as a narrow source-level parity guard for the six frozen shared AE GameTest scenarios.
- The guard reads the common scenario source files plus `MineAgentAeFabricGameTestEntrypoint` and the Forge bootstrap/wrapper source files directly, so it catches loader-exposure drift without needing a generic registry or loader-runtime bootstrapping.

## 2026-04-23T00:00:00Z Task 5: AE naming and parity normalization
- Normalized the checked-in parity snapshot so the Fabric and Forge AE inventories both show the full six-scenario shared surface, including `ae_smoke_binding_unavailable` on Fabric and `mineagentae` on Forge.
- The report artifacts now mirror the frozen Wave 1 contract instead of the earlier five-scenario AE snapshot.

## 2026-04-23T00:00:00Z Task 6: AE terminal list pagination regressions
- `AiTerminalPartOperations.listItems(...)` had two observable list-contract gaps before this task: it only populated `craftable=true` flags when `craftableOnly` was already enabled, and its page tokens advanced over the raw inventory list instead of the visible craftable-only subset.
- A small pure-Java pagination seam lets Wave 2 regressions lock down list behavior in plain JUnit without depending on AE2 runtime classes being present on the common-module test runtime classpath.
- The list contract is now frozen around case-insensitive trimmed query filtering, item-id sorting, lower/upper limit clamps (`1..200`), null/blank/invalid page-token fallback to the first page, and stable next-page tokens computed from the filtered visible result set.

## 2026-04-23T00:00:00Z Task 6 follow-up: negative numeric token handling
- Numeric page tokens also need semantic validation, not just parse validation: `-1` must fall back like any other invalid token instead of surviving to `subList(...)` as a negative offset.

## 2026-04-23T00:00:00Z Task 7: AE terminal job lifecycle regressions
- A second pure-Java seam was needed for `AiTerminalPartOperations` because plain common-module JUnit still cannot safely discover AE2-dependent test doubles, but it can reliably execute stateful regressions against a package-private generic lifecycle helper in the same package.
- The frozen observable lifecycle contract now covers deterministic `resolveKey(...)` parsing/availability outcomes, case-insensitive substring CPU selection, `Job not found` handling for missing jobs, `Missing items: ...` simulation failures, done/canceled refresh transitions, and live-job removal after `clearJobs()`.

## 2026-04-23T04:27:00Z Task 8: AE terminal context resolver regressions
- `AeTerminalContextResolver` also needed the same plain-JUnit strategy as Tasks 6 and 7: keep AE2/Minecraft adaptation in the real resolver, but freeze success/invalidation semantics behind a package-private helper that only depends on small local interfaces and `TerminalBinding`.
- The observable resolver contract is now locked around `Optional` presence only: live AE terminal menus resolve, while null/wrong-menu/missing-host/wrong-host/removed-host cases stay empty; binding lookup succeeds for the matching side and stays empty for wrong-side, invalid-dimension, missing-block-entity, and removed-host cases.

## 2026-04-23T04:45:00Z Task 9: AE tool output renderer regressions
- `AeToolOutputRenderer` now has explicit renderer-contract coverage for list-item lines, missing-item lines, pagination text, `TOOL_RESULT_MAX` truncation overflow, empty AE-list results, and malformed payload fallthrough.
- In this module's plain JUnit runtime, Minecraft-linked item-tag formatting can be absent even though the renderer is in a common module, so deterministic renderer tests need either raw-id fallback behavior or a test seam that does not hard-require `ToolOutputFormatter`'s Minecraft classes.
- Empty list payloads are only safe to treat as AE results when the payload also carries AE list metadata (`nextPageToken` and/or `error`), which avoids hijacking ambiguous bare `{"results":[]}` payloads from unrelated renderers.
- Tightened the test seam so `AeTerminalContextResolverRegressionTest` calls package-private resolver entrypoints (`fromPlayerMenuState(...)`, `fromPlayerBindingLookup(...)`) instead of the raw helper, keeping the regression anchored to the resolver contract while still avoiding AE2-dependent JUnit setup.

## 2026-04-23T04:53:00Z Task 10: AE init wiring regression
- Split `MineAgentAe.init()` into a package-private `initCommonWiring()` helper so common-module JUnit can verify provider/group/resolver/renderer registration without loading AE2 part registries.
- The regression now locks the `MineAgentAe.init()` delegation contract with a source-level check for `MineAgentAePartRegistries.init()` plus the model registration call, because the part registries themselves still require AE2/runtime classes that are absent from plain common JUnit.
- The new runtime-safe assertions proved the AE tool provider is registered under `ae`, the group id resolves to `mineagentae`, the terminal context resolver is installed, and the AE renderer handles job-status payloads deterministically.

## 2026-04-23T05:06:00Z Task 11: AE terminal removal runtime lifecycle
- Added a seventh shared AE runtime scenario, `terminalRemovalInvalidatesMenuContextAndClearsJobs(...)`, to `AeCraftLifecycleIsolationGameTestScenarios` so Wave 3 now covers teardown-driven job clearing plus menu/context invalidation in one loader-visible path.
- The shared scenario proves three observable outcomes together: a live request reaches `submitted`, teardown clears `getRequestedJobs()`, `AeTerminalContextResolver.fromPlayer(...)` and `AiTerminalMenu.stillValid(...)` both go dead after removal, and stale context queries fail as `Job not found` / `AE2 network not connected` instead of silently succeeding.
- Fabric AE smoke and Forge AE GameTests now both execute seven shared AE scenarios, so the terminal-removal lifecycle coverage stays parity-aligned with the existing `mineagentae`/`ae_smoke_*` surfaces.

## 2026-04-22T19:26:07Z Task 12: binding re-resolution runtime coverage
- Added an eighth shared AE runtime scenario, `bindingBasedContextReresolutionSucceedsUntilBindingBecomesStale(...)`, to `AeBindingFailureGameTestScenarios` so Wave 3 now proves the positive binding re-resolution path as well as the stale-binding empty-result path.
- The shared scenario keeps the contract narrow and loader-visible: an AE menu host is first valid, `fromPlayer(...)` goes empty after switching back to a non-AE menu, `fromPlayerAtBinding(...)` re-resolves the live host through the real world block-entity lookup, and the same binding turns empty again once the bound side is cleared.
- Fabric AE smoke and Forge AE GameTests now both execute eight shared AE scenarios, and the parity guard was updated so this binding re-resolution surface cannot drift out of loader exposure.

## 2026-04-23T05:41:30Z Task 13: cancel/clear isolation runtime coverage
- Added a ninth shared AE runtime scenario, `cancelAndClearStayTerminalLocalAfterSubmittedRequest(...)`, to `AeCraftLifecycleIsolationGameTestScenarios` so Wave 3 now covers cancel/clear job lifecycle edges without reusing the Task 11 removal path or the Task 12 binding path.
- The shared scenario freezes one coherent observable contract: two terminals can hold separate submitted jobs at once, canceling one terminal's job moves only that job to `canceled` and clears only that terminal's live-request set, and a later `clearJobs()` turns only that canceled job into `unknown`/`Job not found` while the sibling terminal stays `submitted`.
- Fabric AE smoke and Forge AE GameTests now both execute nine shared AE scenarios, with the new Fabric `ae_smoke_cancel_clear_isolation` batch and the Forge `AeCancelClearIsolationGameTest` wrapper preserving the existing thin-loader pattern.

## 2026-04-23T06:00:00Z Task 14: nightly Forge AE rollout wiring
- The nightly CI collector already accepts Forge log, Forge XML report, and Forge exit-code inputs, so the AE Forge rollout can stay isolated to `.github/workflows/layered-testing.yml` without adding new parsing code.
- Nightly AE Forge evidence is captured under `ci-reports/nightly/ext-ae-forge-gametest.log` and `ext-ae/forge-1.20.1/build/reports/**/*.xml`, keeping the rollout visible in the nightly summary/artifact set while PR/dev lanes remain unchanged.
