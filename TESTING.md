# Testing

Use JDK 25 and Minecraft 26.3 for every run.

| Change | Test tier | Location |
| --- | --- | --- |
| Value object, catalog, identity, policy, or persisted settings | Shared JVM test | `common/src/test/java` |
| Shared loaded-client, UI, key, sound-Mixin, or rendering contract | Shared client assertion | `common/src/clientTest/java` |
| Fabric callback or production artifact behavior | Fabric client GameTest | `fabric/src/gametest/java` |
| NeoForge callback, sound replacement, or production artifact behavior | NeoForge client harness | `neoforge/src/clientTest/java` |

Keep fixtures local and never commit runtime state from `run/`.

## Automated commands

```sh
./gradlew test
./gradlew build
./gradlew runProductionClientGameTest
```

- `test` delegates to `:common:test`.
- `build` runs the shared JVM suite, builds both loaders, and runs configured server GameTests.
- `runProductionClientGameTest` runs Fabric's production JAR and test mod; it uses Xvfb automatically on Linux.
- CI additionally stages `neoforge/build/libs/*-neoforge.jar` and `*-neoforge-client-test.jar` into MC-Runtime-Test. The job fails unless the harness writes `run/noisy-neighbors-neoforge-success` after all assertions and rendered frames pass.

Run development clients separately:

```sh
./gradlew runFabricClient
./gradlew runNeoForgeClient
```

They use `run/fabric` and `run/neoforge`. Copy only `config/noisy-neighbors.json` between them when checking settings portability.

## Automated client coverage

Both loader clients verify initializer/catalog state, K/Z/U defaults, the real loader tick callback, world identity, UI search and fine-grained controls, sound rejection before channel/listener allocation, loader interaction/disconnect wiring, and zone-outline extraction/rendering without a crash. NeoForge also replaces an allowed sound through `PlaySoundEvent` and verifies the replacement is rejected by the sound policy.

## Artifact inspection

The build must produce exactly:

- `fabric/build/libs/noisy-neighbors-<version>-mc26.3-fabric.jar`
- `neoforge/build/libs/noisy-neighbors-<version>-mc26.3-neoforge.jar`

The Fabric JAR must contain `fabric.mod.json`; the NeoForge JAR must contain `META-INF/neoforge.mods.toml`. Both must contain the shared classes, Mixin config, and assets. Sources, dev, GameTest, and client-test JARs are not release artifacts.

Human-perceived audio volume and pixel-perfect outline appearance remain manual checks in both development clients.
