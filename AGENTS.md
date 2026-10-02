# Noisy Neighbors Agent Guide

Follow the repository instructions inherited from the parent dotfiles repository.

## Tests

Read [`TESTING.md`](TESTING.md) before changing behavior. Every behavior change needs a focused regression test in the smallest applicable tier, and `./gradlew test` must pass. Run `./gradlew build` for Fabric/runtime changes; run `./gradlew runProductionClientGameTest` when changing client lifecycle, Fabric events, or mixins.
