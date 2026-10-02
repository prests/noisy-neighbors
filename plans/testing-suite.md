# Test Suite Plan

## Context
- Branch `feat/mod/scaffold` adds the client-only Fabric 26.3 / Java 25 mod features: local settings persistence, world/zone editing, global and zone volume policy, and mixins for UI, sound, server/world list behavior, selection, and rendering.
- Current test support is a single assertion-driven `FocusedCheck` under `src/test/java`, run by a custom Gradle `JavaExec` task. `test` deliberately permits no discovered tests.
- Goal: replace that thin check with a maintainable unit and meaningful integration test strategy, then run build and tests on pull requests and pushes to `main`.

## Approach
- Use three Gradle-run tiers pinned to **Java 25 / Minecraft 26.3**: fast JUnit 5 unit tests for pure domain behavior; JUnit integration tests that use a temporary real JSON settings file and compose state, policy, and identity; Fabric GameTests for the few runtime contracts that require a loaded Minecraft/Fabric client/server.
- Replace the assertion-only `focusedCheck` execution path with standard JUnit Platform reporting. Add Fabric Loader's JUnit integration so Mixin/runtime assumptions are safe in JVM tests. Configure Fabric API's dedicated `gametest` source set/test mod for GameTests; run client GameTests headlessly in CI (with Fabric's network-synchronizer workaround if still required by the pinned versions).
- Update the existing GitHub Actions workflow for `pull_request` and pushes to `main`: set up JDK 25, run the complete Gradle verification/build commands, and leave artifacts out for now.

### Research basis
- Fabric's [automatic testing guide](https://docs.fabricmc.net/develop/automatic-testing) recommends Fabric Loader JUnit for unit tests and GameTest for in-game behavior; it documents separate `gametest` source sets/test mods plus server/client GameTest execution.
- Fabric notes ordinary JUnit does not automatically provide the Mixin-aware environment, and client GameTests may need `-Dfabric.client.gametest.disableNetworkSynchronizer=true` in CI. Version-specific DSL names will be verified against this repo's pinned Loom/Fabric API before implementation.
- JUnit Jupiter's [`@TempDir`](https://docs.junit.org/5.14.1/writing-tests/built-in-extensions.html) supports isolated, automatically cleaned filesystem fixtures—appropriate for persistence integrations.

## Files to modify
- `build.gradle` — replace the no-test guard/custom `focusedCheck` task with JUnit Platform; add `fabric-loader-junit`; configure Loom/Fabric API GameTest source set and the production client GameTest task (including the Linux/XVFB CI setting).
- `src/test/java/com/github/prests/noisyneighbors/**` — JUnit unit and JVM integration tests; replace `FocusedCheck` rather than retaining a parallel assertion runner.
- `src/gametest/java/com/github/prests/noisyneighbors/**` and `src/gametest/resources/fabric.mod.json` — minimal test mod and one client GameTest entrypoint for actual client/world lifecycle coverage.
- `src/main/java/.../client/config/SettingsStore.java` and the smallest adjacent domain helpers required to inject a settings path/reset state and drive state changes without GUI/Minecraft statics.
- `TESTING.md` and root `AGENTS.md` — short test contract, linked from agent instructions: tier-selection table, fixture rules, required commands, and regression-test rule for behavior changes while retaining the inherited dotfiles conventions.
- `.github/workflows/build.yml` — retain the existing workflow, narrow `push` to `main`, remove JAR upload, and add the client GameTest command after build.

## Reuse
- `client/config/SettingsStore.java`, `WorldIdentity.java`, `client/sound/VolumePolicy.java`, and `client/zone/Zone.java` are the core seams. `Zone` already normalizes bounds, supplies containment, sanitizes name/color, and calculates enabled-zone multipliers.
- `VolumePolicy.multiplier(event, sourceMob, world, dimension, x, y, z)` composes catalog lookup, global settings, world zones, dimension filtering, and geometry; it is the natural integration boundary for mocked sound playback.
- Existing `src/test/java/.../FocusedCheck.java` covers identity isolation, zone normalization/containment, overlap multiplication, and catalog classification but is a manual `main` with Java `assert`; migrate those cases to JUnit.
- `SettingsStore` currently owns a static Fabric-derived file path and static data. Testable persistence will require the smallest seam: injected/configurable path plus reset/loadable store state, rather than filesystem or Fabric-loader mocking.

## Test tiers and required coverage

### Unit tests — deterministic, no file or live game
- `ZoneTest`: normalize reversed/negative bounds; include/exclude fractional block edges and same-Y height; default/trimmed name; color masking/default; enabled/disabled/missing volume behavior.
- `WorldIdentityTest`: local-save isolation; server endpoint trim/case normalization; display name never changes identity.
- `MobSoundCatalogTest`: known catalog mapping, shared event requires source, mismatched source rejects event, unknown event passes through, invalid/ambiguous catalog fails closed.
- `VolumePolicyTest`: global-only, zone-only, multiplicative overlaps, zero mute, disabled zone, foreign dimension/world, source mismatch/player/unknown passthrough, and original-sound-volume multiplication at the narrow sound adapter seam.
- Small mutation helpers extracted from UI/selection code (rename, recolor, replace bounds, set volume) get direct unit tests only if extraction is necessary; do not instantiate screens or mock widgets.

### JVM integration tests — real serialized state and multiple systems
Use `@TempDir` and a fresh explicit `SettingsStore` instance/path per case; load the real catalog once. Each flow mutates through production operations, saves, constructs a new store, reloads, and asserts both persisted state and `VolumePolicy` results.
- **Local world lifecycle:** create a local-world record and zone; set global volume; set zone volume/color/name/bounds; verify normalized bounds and effective mocked cow-sound volume inside vs outside; reload; delete that world; reload again and verify only its state is gone.
- **Server lifecycle:** normalize/join a server identity; create an Overworld zone and state; reload/rejoin using equivalent endpoint spelling; verify state applies; delete the server record; reload and verify it is absent without affecting a local world.
- **Zone lifecycle:** create multiple zones; edit one zone's volume/color/name/dimensions; assert only that ID changed, overlap multiplication is correct, cross-dimension sound is unchanged, and deleting one zone preserves the other.
- **Persistence recovery:** missing file initializes empty; malformed/unsupported JSON is ignored without replacing known in-memory state; invalid stored values are filtered; atomic-save fallback remains covered through a filesystem seam only if it can be deterministic (do not mock `Files` or assert logging).

### Fabric GameTest — runtime wiring only
- Configure the Fabric-generated `gametest` test mod and register one client test under `fabric-client-gametest`.
- Create a disposable singleplayer test world, wait for rendered chunks, then exercise the production client lifecycle/event registration and a real world/dimension context sufficient to prove the test mod and client mixins load. Take no screenshot/artifact assertion; success is startup, world creation, and clean teardown.
- Keep gameplay/business-rule permutations in JVM integration tests. A GameTest must cover a runtime-only contract (Fabric event, Mixin, actual client/world lifecycle); do not duplicate JUnit cases or automate GUI clicks/audio hardware output.

## Steps
- [x] Refactor only the settings/state boundary needed for isolated stores and production state mutations; retain the client singleton as a thin production facade, never fake Fabric/Minecraft filesystem APIs.
- [x] Convert `FocusedCheck` assertions into named JUnit Jupiter tests and add the unit suite above, including a small mocked-sound adapter assertion for final original-volume × policy multiplier.
- [x] Add the three real-file integration flows and persistence-recovery cases; use immutable IDs and `@TempDir`, not shared static state or committed config fixtures.
- [x] Configure Fabric Loader JUnit, Fabric API GameTests, `src/gametest` test metadata, and a single headless production client GameTest. Run server GameTests via `build` and client GameTests through `runProductionClientGameTest` with XVFB; include Fabric's network-synchronizer flag only when the pinned runtime exhibits that documented CI failure.
- [x] Add a concise root `AGENTS.md` that points agents to `TESTING.md`; document tier selection, fixture rules, required commands, and the requirement to add a regression test for every behavior change.
- [x] Update existing CI to run on PRs and pushes to `main` only, executing `./gradlew build` then `./gradlew runProductionClientGameTest`, with no upload step.

## Verification
- `./gradlew test` — all JUnit unit/integration tests, with standard XML/HTML Gradle reports on failure.
- `./gradlew build` — compiles/packages the mod and runs server GameTests as configured by Fabric.
- `./gradlew runProductionClientGameTest` — production-style client test under local display/XVFB; run with the documented network-synchronizer system property only if necessary for 26.3 CI.
- In a clean checkout/Actions runner, the workflow runs `build` and the production client GameTest with JDK 25; test failures fail the PR or `main` push.
- No manual audio-output assertion: CI proves the catalog-to-policy-to-saved-state decision and runtime loading; manual `runClient` remains the only appropriate check for human-perceived loudness.

## Decisions
- CI targets only the current Java 25 / Minecraft 26.3 branch. Future maintained Minecraft versions receive their own branch-specific workflow/configuration.
- Headless Fabric/Minecraft GameTests are acceptable in GitHub Actions.
- CI is pass/fail only; do not upload artifacts yet.
