# Testing

Use JDK 25 for Gradle and Minecraft 1.21.1 for every run. The compiled mod targets Java 21, so players can use Java 21 or newer.

```sh
./gradlew test
./gradlew build
./gradlew runClient
```

`test` covers catalog, policy, world identity, zones, and settings persistence. `build` also remaps the distributable JAR to `build/libs/`. The Fabric API version used by this project has no client GameTest harness, so test sound interception, screens, controls, and rendering in `runClient`.

## Manual client smoke test

- Open **Options → Music & Sound → Advanced Sound Controls**; verify search, sliders, advanced controls, keyboard focus, and Escape/Done navigation.
- Set a cow to zero volume and zero chattiness, then restore normal values.
- Create overlapping zones; test inside/outside volume and chattiness, rename, recolor, edit bounds, toggle, delete, and outlines in each dimension.
- Test remappable **K**, **Z**, and **U** controls, reconnect/disconnect, world/server separation, restart persistence, and a multiplayer server without the mod installed.
- Load and save a settings file containing newer-version entries; they must remain in JSON but not appear in the 1.21.1 controls.

## IntelliJ IDEA

1. Install a JDK 25 and set it as both **Project SDK** and **Gradle JVM**. Loom 1.19.3 requires Java 25 even though the mod is compiled for Java 21.
2. Choose **File → Open**, select this repository's `build.gradle`, then choose **Open as Project**. Let IntelliJ import the Gradle project and download sources.
3. In the Gradle tool window, run `Tasks → fabric → runClient` (or create a Gradle run configuration for `runClient`). This creates and launches the 1.21.1 development client using the `run/` directory.
4. Run `Tasks → verification → test` for JVM tests and `Tasks → build → build` for the remapped JAR. Use the Gradle task, rather than IntelliJ's plain Application runner, so Fabric's generated classpath and run configuration are used.
5. If the IDE reports stale mappings after changing Gradle files, click **Reload All Gradle Projects** and rerun `runClient`.
