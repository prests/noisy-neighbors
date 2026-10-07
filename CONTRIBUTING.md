# Contributing to Noisy Neighbors

Thanks for helping improve Noisy Neighbors! We appreciate you wanting to get involved.

## Compatibility

- Minecraft Java Edition 26.3
- Fabric Loader 0.19.5
- Java 25
- Fabric API 0.161.0+26.3

Fabric is the only supported loader.

## Development

Install JDK 25 and import this directory as a Gradle project. [IntelliJ IDEA](https://www.jetbrains.com/idea/) is recommended by Fabric; VS Code is also supported.

```sh
./gradlew runClient
./gradlew test
./gradlew build
./gradlew runProductionClientGameTest
```

`runClient` starts a development client. `build` writes the distributable mod JAR to `build/libs/`.

**Note:** See [Our Testing Doc](TESTING.md) for more information and best practices to ensure proper test coverage.

## Support and end-of-life policy

`main` supports the newest Minecraft release and the newest mod major version. Releases use independent mod and Minecraft versions: for example, `v2.1.0-mc26.3-fabric` is mod version `2.1.0` for Minecraft `26.3`.

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
