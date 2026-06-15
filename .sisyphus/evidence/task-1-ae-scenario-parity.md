# Task 1 AE Shared GameTest Parity Matrix

## Parity matrix

Wave 1 parity rule: every runnable shared AE scenario in common code must have both a Fabric path and a Forge path, following the base-mod thin-loader architecture.

| Shared scenario method | Fabric runtime delegate | Fabric `@GameTest` entrypoint | Forge wrapper | Forge bootstrap registration | Final status |
| --- | --- | --- | --- | --- | --- |
| `AeCraftLifecycleIsolationGameTestScenarios.craftLifecycleIsolation(...)` | Present: `MineAgentAeFabricRuntimeGameTests.craftLifecycleIsolation(...)` | Present: `MineAgentAeFabricGameTestEntrypoint.craftLifecycleIsolation(...)` | Present: `AeCraftLifecycleIsolationGameTest` | Present: `MineAgentAeGameTestBootstrap` includes `AeCraftLifecycleIsolationGameTest.class` | Exposed on both loaders |
| `AeCraftLifecycleIsolationGameTestScenarios.terminalTeardownClearsLiveJobs(...)` | Present: `MineAgentAeFabricRuntimeGameTests.terminalTeardownClearsLiveJobs(...)` | Present: `MineAgentAeFabricGameTestEntrypoint.aeTerminalTeardownClearsLiveJobs(...)` | Present: `AeTerminalTeardownLiveJobsGameTest` | Present: `MineAgentAeGameTestBootstrap` includes `AeTerminalTeardownLiveJobsGameTest.class` | Exposed on both loaders |
| `AeCraftLifecycleIsolationGameTestScenarios.cpuTargetedUnavailableCpuBranch(...)` | Present: `MineAgentAeFabricRuntimeGameTests.cpuTargetedUnavailableCpuBranch(...)` | Present: `MineAgentAeFabricGameTestEntrypoint.aeCpuTargetedUnavailableCpuBranch(...)` | Present: `AeCpuUnavailableGameTest` | Present: `MineAgentAeGameTestBootstrap` includes `AeCpuUnavailableGameTest.class` | Exposed on both loaders |
| `AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` | Present: `MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` | Absent: no method in `MineAgentAeFabricGameTestEntrypoint` yet | Absent: no Forge wrapper class yet | Absent: `MineAgentAeGameTestBootstrap` has nothing to register yet | Orphaned |
| `AeBindingFailureGameTestScenarios.boundTerminalApprovalSuccessHandoff(...)` | Present: `MineAgentAeFabricRuntimeGameTests.boundTerminalApprovalSuccessHandoff(...)` | Present: `MineAgentAeFabricGameTestEntrypoint.aeBoundTerminalApprovalSuccessHandoff(...)` | Present: `AeBoundTerminalApprovalSuccessGameTest` | Present: `MineAgentAeGameTestBootstrap` includes `AeBoundTerminalApprovalSuccessGameTest.class` | Exposed on both loaders |
| `AeBindingFailureGameTestScenarios.bindingInvalidationAfterTerminalRemovalOrWrongSide(...)` | Present: `MineAgentAeFabricRuntimeGameTests.bindingInvalidationAfterTerminalRemovalOrWrongSide(...)` | Present: `MineAgentAeFabricGameTestEntrypoint.aeBindingInvalidationAfterTerminalRemovalOrWrongSide(...)` | Present: `AeBindingInvalidationGameTest` | Present: `MineAgentAeGameTestBootstrap` includes `AeBindingInvalidationGameTest.class` | Exposed on both loaders |

## Frozen parity contract

- Shared scenario surface size for Wave 1: `6`
- Currently exposed on both loaders: `5`
- Currently orphaned: `1`
- Helper-only shared scenarios in this surface: `0`

## Required follow-through for downstream tasks

`AeBindingFailureGameTestScenarios.boundTerminalApprovalFailsWhenAeBindingUnavailable(...)` is not an excluded helper. It is a runnable shared AE scenario and remains in scope for parity.

- Task 2 must add the missing Fabric `@GameTest` entrypoint while keeping the existing runtime delegate thin.
- Task 3 must add the missing Forge thin wrapper and bootstrap registration.
- Task 4 can then guard this six-method inventory automatically.

## Interpretation notes

- “Exposed on both loaders” means Fabric has both the runtime delegate and the loader-facing `@GameTest` entrypoint, and Forge has both the wrapper class and bootstrap registration.
- “Orphaned” means the shared scenario remains runnable in common code but at least one loader-visible surface is missing.
- “Helper-only” would require the method to stop being part of the public runnable shared scenario surface; no current AE shared scenario falls into that category.
