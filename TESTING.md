# Testing

Use JDK 25 for Gradle and Minecraft 1.21.1 for every run. The compiled mod targets Java 21, so players can use Java 21 or newer.

```sh
./gradlew clean
./gradlew :common:test
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew build
```

Run each development client separately:

```sh
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

Fabric uses `run/fabric`; NeoForge uses `run/neoforge`. Keep the directories separate. To test settings portability, copy only `config/noisy-neighbors.json` between them.

Distributable JARs are written to:

- `fabric/build/libs/noisy-neighbors-<version>-mc1.21.1-fabric.jar`
- `neoforge/build/libs/noisy-neighbors-<version>-mc1.21.1-neoforge.jar`

## Manual client smoke test

Run every item on both loaders.

- Reach the main menu with no mod-loading, Mixin, or missing-asset errors.
- Open **Options → Music & Sound → Advanced Sound Controls**; verify search, sliders, advanced controls, keyboard focus, narration, and Escape/Done navigation.
- Verify the three remappable controls appear, then test **K**, **Z**, and **U**.
- Set cow volume and chattiness to zero, test an intermediate value and one per-event value, then restore them.
- Create, edit, overlap, recolor, disable, and delete zones; verify outlines in multiple dimensions and graphics modes.
- Verify zone selection consumes main-hand block use, ignores offhand use, cancels correctly, and resets after disconnect.
- Verify moving sounds retain scaling and Minecraft category-volume changes do not remove Noisy Neighbors scaling.
- Verify local worlds and normalized server addresses remain separate and deleting their entries removes their saved zones.
- Load malformed, unsupported, and newer-version settings and confirm existing safe handling remains intact.
- Connect to a server without Noisy Neighbors installed; no custom handshake should occur.
- Copy a settings file created by one loader to the other and verify it loads unchanged.

On NeoForge, also test with a sound listener or compatible mod that replaces a `SoundInstance` when practical; policy must apply to the replacement sound.

## Artifact inspection

The Fabric JAR must contain `fabric.mod.json` and the Fabric adapter, but no NeoForge metadata or adapter. The NeoForge JAR must contain `META-INF/neoforge.mods.toml` and the NeoForge adapter, but no Fabric metadata or adapter. Both must contain common classes, Mixins, the shared Mixin config, and `assets/noisy-neighbors/**` without bundling loader, Minecraft, Mixin, Gson, or SLF4J dependencies.

## IntelliJ IDEA

1. Install JDK 25 and set it as the Gradle JVM. The source toolchain remains Java 21.
2. Open the repository root as a Gradle project and reload all Gradle projects.
3. Verify the run selector contains **Noisy Neighbors - Fabric Client** and **Noisy Neighbors - NeoForge Client**.
4. Run and debug each configuration. The selected configuration should persist in the selector.
5. If plugin-generated configurations are stale, use the committed `.run/` Gradle configurations or run the module tasks above.
