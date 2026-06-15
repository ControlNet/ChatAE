## 2026-04-22T17:11:06Z Task: session-start
Initial note file for architectural and implementation decisions.

## 2026-04-22T17:17:31Z Task 1: Wave 1 parity contract
- Decision: treat the six public methods in `AeCraftLifecycleIsolationGameTestScenarios` and `AeBindingFailureGameTestScenarios` as the complete in-scope shared AE GameTest surface for Wave 1 unless repository facts change.
- Decision: every in-scope shared AE scenario must be surfaced through both Fabric and Forge; loader code stays thin and may not duplicate scenario bodies.
- Decision: `AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` remains a runnable shared scenario, not a helper-only method, so downstream work must expose it rather than excluding it from parity.

## 2026-04-23T00:00:00Z Task 2: Fabric batch naming
- Decision: place the new Fabric entrypoint in `ae_smoke_binding_unavailable` so the orphaned scenario stays in the AE smoke family without introducing a new lane.

## 2026-04-23T00:00:00Z Task 3: Forge wrapper convention
- Decision: keep the Forge exposure as a one-class thin wrapper with the same `mineagentae` batch/namespace and local `createPlayer(...)` helper used by the existing AE Forge tests.
- Decision: register the new wrapper directly in `MineAgentAeGameTestBootstrap` rather than introducing any additional Forge bootstrap surface.

## 2026-04-23T00:00:00Z Task 4: source-level parity guard shape
- Decision: keep the parity guard as a single common-module JUnit class that freezes the six-scenario inventory and verifies loader exposure by explicit source snippets instead of adding a repo-wide framework.
- Decision: audit Forge in two steps inside the same guard: bootstrap registration must include each wrapper class, and each wrapper source must call the matching shared scenario method.

## 2026-04-23T00:00:00Z Task 5: parity report normalization
- Decision: keep the checked-in parity snapshot aligned with the same six-scenario contract as source, using Fabric `ae_smoke_*` batches and Forge `mineagentae` registration as the canonical naming shape.
- Decision: represent the binding-unavailable AE scenario as a first-class shared surface entry in the report artifacts rather than treating it as a special case.

## 2026-04-23T00:00:00Z Task 6: AE terminal list seam
- Decision: extract the list/pagination rules into package-private `AiTerminalPartOperationsListPagination` in the same package instead of forcing AE2 runtime into plain JUnit; `AiTerminalPartOperations` now maps AE2 state into `AiTerminalData.AeEntry` values and delegates the observable paging contract there.
- Decision: always fetch the craftables set in `listItems(...)` so returned entries can report stable `craftable` flags even when `craftableOnly=false`; filtering and page-token advancement then happen against those fully populated entries.

## 2026-04-23T00:00:00Z Task 7: AE terminal job lifecycle seam
- Decision: keep `AiTerminalPartOperations` as the AE2-facing entrypoint, but move key resolution, CPU name matching, and job-state mutation/refresh logic into package-private generic `AiTerminalPartOperationsJobLifecycle` so plain JUnit can lock down the contract without loading AE2 APIs.
- Decision: freeze Task 7 on externally visible outputs only: stable status strings, exact error messages, missing-item lists, and live-job presence/absence rather than asserting internal map contents.

## 2026-04-23T04:27:00Z Task 8: AE terminal context resolver seam
- Decision: keep `AeTerminalContextResolver` as the only AE2/Minecraft-facing entrypoint, but route its menu/binding Optional resolution through package-private `AeTerminalContextResolution` so the contract can be tested without loading AE2 compile-only interfaces in plain JUnit.
- Decision: the regression class should assert only `Optional` presence/absence for the required success and invalidation scenarios, not `PlayerTerminalContext` internals or adapter implementation details.

## 2026-04-23T04:45:00Z Task 9: renderer contract and fallback seam
- Decision: keep the richer AE renderer coverage split across the existing regression class plus a small companion fallback class so direct line formatting and malformed-payload fallthrough are both explicit without broadening into UI/runtime tests.
- Decision: recognize empty AE list payloads only when `results` is empty and the payload explicitly carries AE list metadata (`nextPageToken` or `error`), preserving fallback behavior for ambiguous bare empty result arrays.
- Decision: add a tiny `AeToolOutputRenderer` item-tag fallback that returns the raw item id when `ToolOutputFormatter.formatItemTag(...)` cannot load Minecraft classes in plain JUnit; production runtime still uses the shared formatter whenever those classes are present.
- Decision: expose package-private resolver seam methods for the regression class and keep `AeTerminalContextResolution` as an implementation detail, so Task 8 freezes the resolver contract rather than only the underlying helper behavior.

## 2026-04-23T04:53:00Z Task 10: init wiring seam
- Decision: add a package-private `MineAgentAe.initCommonWiring()` helper and let `MineAgentAe.init()` call it before part-registry bootstrap, so common JUnit can cover the AE wiring that does not depend on AE2 runtime classes.
- Decision: keep the part-registry bootstrap contract in place via source-level regression checks rather than forcing `MineAgentAePartRegistries` to load in plain common JUnit, because that class still depends on AE2 types that are not available in this module's test runtime.

## 2026-04-23T05:06:00Z Task 11: terminal removal lifecycle shape
- Decision: keep Task 11 in `AeCraftLifecycleIsolationGameTestScenarios` next to `terminalTeardownClearsLiveJobs(...)`, because this work extends the existing teardown/lifecycle runtime suite rather than creating a second lifecycle class.
- Decision: model terminal removal through a shared mutable AE host double that mirrors the real `AiTerminalPart` observable contract (`clearJobs()` plus loss of AE network access) while exercising the real `AiTerminalMenu` and `AeTerminalContextResolver` runtime surfaces.
- Decision: freeze post-removal failure on observable stale-context outcomes only: prior job ids become `unknown` with `Job not found`, and list queries return the same `AE2 network not connected` error shape that `AiTerminalPart` exposes when its grid is gone.

## 2026-04-22T19:26:07Z Task 12: binding re-resolution scenario shape
- Decision: keep Task 12 in `AeBindingFailureGameTestScenarios`, because it extends the shared binding/runtime contract rather than the craft lifecycle family.
- Decision: prove "re-resolution" by explicitly closing the live AE menu (`player.inventoryMenu`) before resolving through `AeTerminalContextResolver.fromPlayerAtBinding(...)`; that keeps the success assertion about binding continuity instead of accidentally reusing the menu-context path.
- Decision: model stale binding as the bound side disappearing from a world-installed mutable part host while leaving the rest of the scenario loader-thin and shared, so the failure stays about binding staleness rather than proposal approval or unrelated world teardown.

## 2026-04-23T05:41:30Z Task 13: cancel/clear isolation scenario shape
- Decision: keep Task 13 in `AeCraftLifecycleIsolationGameTestScenarios`, because it extends the shared request/status lifecycle family around terminal-local job ownership instead of the binding or teardown families.
- Decision: model the new edge as two concurrent submitted requests on separate `AiTerminalPartOperations` instances sharing the same fake player/host, then prove only externally visible behavior: cancel transitions one job to `canceled`, `clearJobs()` turns that same terminal-local job into `unknown`/`Job not found`, and the sibling terminal's job remains `submitted` with its live-request set intact.
- Decision: expose the new shared scenario through the existing thin-loader conventions only: one Fabric runtime delegate + `ae_smoke_cancel_clear_isolation` entrypoint, one Forge wrapper class, and a parity-guard inventory expansion from eight to nine runnable AE scenarios.

## 2026-04-23T06:00:00Z Task 14: nightly-only Forge AE rollout
- Decision: keep the new Forge AE runtime step confined to the nightly lane and wire it into the existing nightly collector instead of introducing any PR/dev exposure or separate summary path.
- Decision: treat the Forge AE runtime log and XML reports as nightly artifacts alongside the current Fabric outputs, using the existing collector contract so nightly failure handling stays explicit and unchanged in spirit.

## 2026-04-23T00:00:00Z Task 15: runtime-doc sync decisions
- Decision: document the ext-AE shared runtime surface as nine loader-visible scenarios by name in `REPO.md`, instead of leaving the older shorter summary that no longer covered Tasks 11, 12, and 13.
- Decision: keep the rollout wording strict and non-promissory, AE Forge GameTests are nightly-only today, PR/dev remain unchanged, and promotion beyond nightly is deferred pending stability.
- Decision: keep the parity snapshot honest about evidence quality by expanding the shared AE inventory now, but marking newer Fabric wrappers as `xml_missing` until checked-in XML evidence catches up.
