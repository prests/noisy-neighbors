# Contributing to Noisy Neighbors

Thanks for helping improve Noisy Neighbors.

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

## Version policy

`main` supports the newest Minecraft release. When a new release becomes the target, an older version gets a maintenance branch (for example, `lts-mc26.3`) only when it needs further fixes. The previous major Minecraft release is supported until a new major version is released. In which case the oldest `lts` version becomes `archived`.

## Contributing a change

1. Fork the repository on GitHub, clone your fork, and create a branch from `main` or an `lts` branch if fixing a bug in an older version.
2. Make the change and run the relevant Gradle commands from [Development](#development) to ensure there are no regressions before creating your pull request.
3. Push the branch to your fork and open a pull request against `prests/noisy-neighbors:main` or `prests/noisy-neighbors:lts-mc<insert-version>` (if targetting an older version of Minecraft).
4. The fork pull request runs the `build` check with a read-only token and no repository or environment secrets. Address review feedback and keep the branch current if GitHub requests it.
5. A project contributor approves the pull request and merges it once the required `build` check passes. Changes under `.github/` also need `@prests` approval.
6. After the merge, wait for a contributor to manually dispatch the release workflow.

Fork contributors cannot access publishing credentials or run releases. Releases are made only when a collaborator decides the merged changes are ready.
