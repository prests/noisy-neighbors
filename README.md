# Noisy Neighbor

A client-only Fabric mod for personal vanilla-mob sound controls. Open **Options → Music & Sound → Advanced Sound Controls** to set per-mob global volume limits; the settings are stored locally in `config/noisy-neighbors.json`.

No server installation is required. Only explicitly catalogued vanilla mob events are changed; player, block, unknown, modded, and mismatched entity sounds remain vanilla. Minecraft cannot distinguish proxy-hosted worlds that share an endpoint and dimension, so those worlds share settings.

## Compatibility

- Minecraft Java Edition 26.3
- Fabric Loader 0.19.5 or later
- Java 25
- Fabric API 0.161.0+26.3

Fabric is the only supported loader.

## Development

Install JDK 25. [IntelliJ IDEA](https://www.jetbrains.com/idea/) is recommended by Fabric; VS Code is also supported. Import this directory as a Gradle project.

```sh
./gradlew runClient
./gradlew build
./gradlew focusedCheck
```

`runClient` starts a development client. `build` writes the distributable mod JAR to `build/libs/`.

## Version policy

`main` supports the newest Minecraft release. When a new release becomes the target, an older version gets a maintenance branch (for example, `mc-26.3`) only when it needs further fixes. Each Minecraft version is released as its own JAR; multi-version build tooling will be added only if maintaining multiple versions becomes necessary.

## License

MIT. See [LICENSE](LICENSE).
