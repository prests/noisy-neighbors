# Noisy Neighbors Agent Guide

## Tests

Read [`TESTING.md`](TESTING.md) before changing behavior. First decide whether the change is purely functional (unit test) or crosses persisted state, runtime boundaries, or multiple systems (integration/GameTest). Update the existing focused test when it already covers the changed flow; add a new test only for a distinct behavior or regression. Keep tests non-repetitive and organized by the production behavior they cover.

Every behavior change needs a focused regression test in the smallest applicable tier, and `./gradlew test` must pass. Run `./gradlew build` for Fabric/runtime changes; run `./gradlew runProductionClientGameTest` when changing client lifecycle, Fabric events, or mixins.
