# Contributing to Noisy Neighbors

Thanks for helping improve Noisy Neighbors.

## Compatibility

- Minecraft Java Edition 26.3
- Fabric Loader 0.19.5 or later
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

`main` supports the newest Minecraft release. When a new release becomes the target, an older version gets a maintenance branch (for example, `mc-26.3`) only when it needs further fixes. Each Minecraft version is released as its own JAR; multi-version build tooling will be added only if maintaining multiple versions becomes necessary.
