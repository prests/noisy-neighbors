# Contributing to Noisy Neighbors

Thanks for helping improve Noisy Neighbors! We appreciate you wanting to get involved.

## Compatibility

- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader 0.20.1 and Fabric API 0.116.17+1.21.1, or NeoForge 21.1.265

## Project structure

- `common/` contains shared gameplay, UI, persistence, assets, tests, and Mixins. It must not import Fabric or NeoForge classes.
- `fabric/` contains only Fabric bootstrap code and metadata.
- `neoforge/` contains only NeoForge bootstrap code and metadata.

Prefer vanilla Minecraft APIs in `common`. Add loader-specific code only for lifecycle, event, rendering, key-registration, or config-path integration that cannot be shared cleanly. Do not duplicate shared classes between loader projects.

## Development

Install JDK 25 for Gradle and import the repository root as a Gradle project. The mod source targets Java 21.

```sh
./gradlew :common:test
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
./gradlew build
```

The clients use separate `run/fabric` and `run/neoforge` directories. IntelliJ should expose **Noisy Neighbors - Fabric Client** and **Noisy Neighbors - NeoForge Client** after Gradle sync; committed `.run/` configurations are provided as a fallback.

Distributable artifacts are written to `fabric/build/libs/*-fabric.jar` and `neoforge/build/libs/*-neoforge.jar`. See [TESTING.md](TESTING.md) for complete verification steps.

## Support and end-of-life policy

`main` supports the newest Minecraft release and the newest mod major version. Source releases use independent mod and Minecraft versions: for example, `v2.1.0-mc26.3` is mod version `2.1.0` for Minecraft `26.3`; published platform versions add a loader suffix.

A long-term-support branch is created only when a supported combination has moved off `main`. Its name is `lts/v<mod-major>.x-mc<minecraft-version>`; for example, `lts/v1.x-mc26.3`. Do not create a branch for the combination currently on `main`.

LTS branches receive compatible bug and security fixes only. New features, new mod majors, and Minecraft ports stay on `main`. Fixes should land on `main` first, then be cherry-picked to each supported lts branch. A branch is not supported until its CI and release workflow can build and publish that branch.

A lts branch becomes archived when either its Minecraft release is no longer in the supported window (essentially as soon as a newer version of Minecraft is released). When archiving a branch the process should be:
- Mark the branch `archived`.
- stop publishing fixes for that branch.
- update the final supported mod/Minecraft release in the relevant release notes across Github, Modrinth, and CurseForge.
  - The note should be: "THIS IS THE FINAL UPDATE FOR THIS MINECRAFT VERSION. PLEASE UPDATE TO THE LATEST MINECRAFT VERSION FOR FUTURE FEATURES AND BUG FIXES."

Tags and published JARs remain available; users should upgrade to the supported line.

## Contributing a change

1. Fork the repository on GitHub, clone your fork, and create a branch from `main` or an `lts` branch if fixing a bug in an older version.
2. Make the change and run the relevant Gradle commands from [Development](#development) to ensure there are no regressions before creating your pull request.
3. Push the branch to your fork and open a pull request against `prests/noisy-neighbors:main` or `prests/noisy-neighbors:lts-mc<insert-version>` (if targeting an older version of Minecraft).
4. The fork pull request runs the `build` check with a read-only token and no repository or environment secrets. Address review feedback and keep the branch current if GitHub requests it.
5. A project contributor approves the pull request and merges it once the required `build` check passes. Changes under `.github/` also need `@prests` approval.
6. After the merge, wait for a contributor to manually dispatch the release workflow.

Fork contributors cannot access publishing credentials or run releases. Releases are made only when a collaborator decides the merged changes are ready.
