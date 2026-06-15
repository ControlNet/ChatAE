# ChatMC / MineAgent project understanding

Date: 2026-06-16

## Identity

This repository is the MineAgent Minecraft 1.20.1 mod workspace. It is a Java 17, Gradle wrapper, Architectury/Loom multi-loader project.

Main product:
- Base mod: `mineagent`
- AE2 extension: `mineagentae`
- Matrix extension scaffold: `mineagentmatrix`

The project currently targets Fabric and Forge for Minecraft 1.20.1. Release artifacts are built by `scripts/build-dist.sh` and copied to `dist/`.

## Module map

- `base/core`: pure Java domain logic. No Minecraft, AE2, Architectury, Fabric, or Forge runtime dependencies.
- `base/common-1.20.1`: shared Minecraft-facing base implementation. Holds common init, networking, UI, recipes, tools, sessions persistence, commands, prompt/config runtime.
- `base/fabric-1.20.1`: Fabric bootstrap and run configurations for base.
- `base/forge-1.20.1`: Forge bootstrap and run configurations for base.
- `ext-ae/core`: pure Java AE DTOs/policy/proposal helpers.
- `ext-ae/common-1.20.1`: AE2 common implementation, AI Terminal part, AE terminal context, AE tools, AE renderers, shared GameTest scenarios.
- `ext-ae/fabric-1.20.1`: Fabric bootstrap and AE Fabric GameTest wrappers.
- `ext-ae/forge-1.20.1`: Forge bootstrap and AE Forge GameTest wrappers.
- `ext-matrix/*`: Matrix extension scaffold, currently much thinner than base and AE.

## Runtime wiring

Loader entrypoints call common initializers:
- Fabric base: `base/fabric-1.20.1/.../MineAgentFabric.java` -> `MineAgent.init()`
- Forge base: `base/forge-1.20.1/.../MineAgentForge.java` -> `MineAgent.init()`
- AE common initializer: `ext-ae/common-1.20.1/.../MineAgentAe.java`

`MineAgent.init()`:
- initializes registries
- registers tool providers `mc` and `http`
- registers tool output renderers for MC and HTTP tools
- initializes network packets and commands
- registers the recipe index reload listener
- on server start: stores server reference, loads prompt runtime, reloads LLM config/runtime, reloads MCP runtime, rebuilds recipe index async
- on server stop: clears runtime state, shuts down recipe index and network executor

`MineAgentAe.init()`:
- registers AE tool provider under provider id `ae`
- registers AE terminal context resolver
- registers AE tool output renderer
- initializes AE part registries

## Core behavior

Sessions:
- Core session state lives in `ServerSessionManager`, `SessionSnapshot`, `SessionMetadata`, and related records.
- Sessions are server-authoritative, persisted via `MineAgentSessionsSavedData` in common code.
- State machine includes `IDLE`, `INDEXING`, `THINKING`, `WAIT_APPROVAL`, `EXECUTING`, `DONE`, `FAILED`, and `CANCELED`.
- `tryStartThinking` enforces the one-in-flight request rule by allowing new work only from idle-like states and when no proposal is pending.

Agent loop:
- `base/core/.../AgentLoop.java` uses LangGraph4j with `reason`, `execute`, and `respond` nodes.
- MC-specific invocation is wrapped by `base/common-1.20.1/.../AgentRunner.java`.
- LLM runtime lifecycle is configured by core `LlmRuntimeManager` and common `McRuntimeManager`.
- The repository guidance says the project currently supports only OpenAI-compatible configuration; `baseUrl` is an endpoint override, not a provider switch.

Tools:
- Tool registration is centralized in `base/common-1.20.1/.../ToolRegistry.java`.
- Base `mc.*` tools are in `McToolProvider`: `mc.find_recipes`, `mc.find_usage`, plus `response`.
- AE tools are in `AeToolProvider`: `ae.list_items`, `ae.list_craftables`, `ae.simulate_craft`, `ae.request_craft`, `ae.job_status`, `ae.job_cancel`.
- AE write-like operations are policy checked by `AeToolPolicy` and may return proposals requiring approval.
- Tool execution uses terminal context resolution, so AE tools require a resolvable `AeTerminalContext`.

Recipes:
- `RecipeIndexService` extracts Minecraft recipes into pure-core `RecipeIndexSnapshot` structures.
- Core search and pagination are delegated to `RecipeIndexManager` and `RecipeSearchAlgorithm`.
- `mc.find_recipes` and `mc.find_usage` return `index_not_ready` until the async index is ready.

Networking/UI:
- `MineAgentNetwork` registers C2S packets for chat, approval, session list, open/create/delete/update session.
- It registers S2C packets for session snapshots, session list, and tool catalog.
- Client-side state is stored through core client stores such as `ClientSessionStore`, `ClientSessionIndex`, and `ClientToolCatalog`.

## Build and verification entrypoints

Use Java 17 and the Gradle wrapper.

Narrow JUnit commands:
```bash
./gradlew --no-daemon --configure-on-demand :base:core:test
./gradlew --no-daemon --configure-on-demand :base:common-1.20.1:test
./gradlew --no-daemon --configure-on-demand :ext-ae:core:test
./gradlew --no-daemon --configure-on-demand :ext-ae:common-1.20.1:test
```

Aggregate JUnit coverage:
```bash
./gradlew --no-daemon --configure-on-demand jacocoUnitTestReport
```

Packaging:
```bash
./scripts/build-dist.sh
```

GameTest commands are slower and loader-specific. Prefer targeted JUnit first unless changing runtime/loader/server-thread/lifecycle behavior.

## Current worktree note

At the time this knowledge was recorded, branch `dev` had existing uncommitted changes in `.sisyphus`, `.omo`, `REPO.md`, CI reports/docs, and multiple `ext-ae` source/test files. Treat these as user/other-agent work unless explicitly told otherwise.
