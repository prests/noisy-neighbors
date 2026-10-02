# Testing

Use the smallest test tier that exercises the changed behavior:

| Change | Add test under | Rule |
| --- | --- | --- |
| Value object, catalog, identity, policy branch | `src/test/java` unit test | No files, Minecraft client, widgets, or mocks of Fabric APIs. |
| Saved settings plus world/zone/policy flow | `src/test/java` integration test | Use `@TempDir`, `SettingsStore.at(path)`, a fresh store reload, and real JSON. |
| Mixin, Fabric callback, or loaded client/world contract | `src/gametest/java` client GameTest | Cover one runtime-only contract; do not duplicate JVM tests or assert speaker output. |

Every behavior change requires a focused regression test. Keep test names descriptive and fixtures local to the test; never commit `run/` state or share mutable static settings between tests.

```sh
./gradlew test
./gradlew build
./gradlew runProductionClientGameTest
```

`build` runs server GameTests. The production client GameTest uses Xvfb automatically on Linux CI. Human-perceived audio volume remains a manual `./gradlew runClient` check; automated tests assert the saved catalog-to-policy decision.
