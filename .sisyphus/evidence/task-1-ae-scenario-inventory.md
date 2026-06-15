# Task 1 AE Shared GameTest Scenario Inventory

## Scope freeze

Task 1 is limited to the shared AE GameTest surface implemented in these two common classes:

- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeCraftLifecycleIsolationGameTestScenarios.java`
- `ext-ae/common-1.20.1/src/main/java/space/controlnet/mineagent/ae/common/gametest/AeBindingFailureGameTestScenarios.java`

Repository inspection shows exactly six runnable shared scenario methods in that surface. This inventory is the Wave 1 parity source of truth.

## Architectural reference

The base-mod pattern remains the contract for AE runtime tests:

- shared scenario bodies live in common scenario classes
- Fabric exposes them through thin runtime delegates plus thin `@GameTest` entrypoints
- Forge exposes them through thin wrapper classes registered in the bootstrap list

Reference files inspected:

- `base/common-1.20.1/src/main/java/space/controlnet/mineagent/common/gametest/ProposalBindingUnavailableGameTestScenarios.java`
- `base/fabric-1.20.1/src/main/java/space/controlnet/mineagent/fabric/gametest/MineAgentFabricRuntimeGameTests.java`
- `base/forge-1.20.1/src/main/java/space/controlnet/mineagent/forge/gametest/MineAgentGameTestBootstrap.java`

## Finite shared scenario inventory

| Shared scenario class | Method | Current responsibility | Wave 1 in-scope? | Fabric exposure target | Forge exposure target |
| --- | --- | --- | --- | --- | --- |
| `AeCraftLifecycleIsolationGameTestScenarios` | `craftLifecycleIsolation` | Craft lifecycle isolation across terminals | Yes | Existing `MineAgentAeFabricRuntimeGameTests.craftLifecycleIsolation(...)` + existing `MineAgentAeFabricGameTestEntrypoint.craftLifecycleIsolation(...)` | Existing `AeCraftLifecycleIsolationGameTest` wrapper + existing `MineAgentAeGameTestBootstrap` registration |
| `AeCraftLifecycleIsolationGameTestScenarios` | `terminalTeardownClearsLiveJobs` | Terminal teardown clears live job tracking | Yes | Existing `MineAgentAeFabricRuntimeGameTests.terminalTeardownClearsLiveJobs(...)` + existing `MineAgentAeFabricGameTestEntrypoint.aeTerminalTeardownClearsLiveJobs(...)` | Existing `AeTerminalTeardownLiveJobsGameTest` wrapper + existing `MineAgentAeGameTestBootstrap` registration |
| `AeCraftLifecycleIsolationGameTestScenarios` | `cpuTargetedUnavailableCpuBranch` | CPU-targeted unavailable branch fails deterministically | Yes | Existing `MineAgentAeFabricRuntimeGameTests.cpuTargetedUnavailableCpuBranch(...)` + existing `MineAgentAeFabricGameTestEntrypoint.aeCpuTargetedUnavailableCpuBranch(...)` | Existing `AeCpuUnavailableGameTest` wrapper + existing `MineAgentAeGameTestBootstrap` registration |
| `AeBindingFailureGameTestScenarios` | `boundTerminalApprovalFailsWhenAeBindingUnavailable` | Approval failure when bound AE terminal is unavailable | Yes | Existing runtime delegate `MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` plus a missing thin `@GameTest` entrypoint in `MineAgentAeFabricGameTestEntrypoint` that must stay in the `ae_smoke_*` family | Missing thin Forge wrapper under `ext-ae/forge-1.20.1/.../gametest/` plus matching `MineAgentAeGameTestBootstrap` registration |
| `AeBindingFailureGameTestScenarios` | `boundTerminalApprovalSuccessHandoff` | Approval success hands work into the AE terminal context | Yes | Existing `MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalSuccessHandoff(...)` + existing `MineAgentAeFabricGameTestEntrypoint.aeBoundTerminalApprovalSuccessHandoff(...)` | Existing `AeBoundTerminalApprovalSuccessGameTest` wrapper + existing `MineAgentAeGameTestBootstrap` registration |
| `AeBindingFailureGameTestScenarios` | `bindingInvalidationAfterTerminalRemovalOrWrongSide` | Terminal removal or wrong-side binding invalidates resolution | Yes | Existing `MineAgentAeFabricRuntimeGameTests.bindingInvalidationAfterTerminalRemovalOrWrongSide(...)` + existing `MineAgentAeFabricGameTestEntrypoint.aeBindingInvalidationAfterTerminalRemovalOrWrongSide(...)` | Existing `AeBindingInvalidationGameTest` wrapper + existing `MineAgentAeGameTestBootstrap` registration |

## Contract freeze for Wave 1

1. These six public shared scenario methods are the complete in-scope AE shared GameTest surface for Wave 1.
2. Every in-scope shared AE scenario must remain implemented in common code and must be surfaced through both Fabric and Forge.
3. Loader code stays thin only:
   - Fabric runtime delegates call common scenarios
   - Fabric entrypoints call Fabric runtime delegates
   - Forge wrappers call common scenarios and are registered by `MineAgentAeGameTestBootstrap`
4. If any future method in these classes is intentionally helper-only rather than runnable, it must be converted out of the public shared scenario surface or explicitly documented as helper-only instead of being left loader-orphaned.

## Current orphan callout

`AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` is currently orphaned from loader-visible AE GameTest surfaces.

- Fabric status: runtime delegate exists in `MineAgentAeFabricRuntimeGameTests`, but no matching `@GameTest` entrypoint exists yet in `MineAgentAeFabricGameTestEntrypoint`
- Forge status: no thin wrapper class exists yet under `ext-ae/forge-1.20.1/.../gametest/`, so `MineAgentAeGameTestBootstrap` cannot register it
- Plan impact: this scenario stays IN scope and must be exposed by Task 2 (Fabric) and Task 3 (Forge)

## Verification notes

- `grep` over `ext-ae/common-1.20.1/.../gametest` for `public static void` returns all six methods listed above.
- Loader inspection shows five currently exposed on both loaders and one known orphan pending Task 2 plus Task 3.
