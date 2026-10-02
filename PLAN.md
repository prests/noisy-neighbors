# GitHub Release Pipeline Plan

## Context
- `noisy-neighbors` is a Fabric client mod for Minecraft `26.3`.
- The request is to research the appropriate publication route and add a GitHub release pipeline triggered by merges to `main`, with release notes based on the merged PR title and description.

## Findings so far
- The repository is `prests/noisy-neighbors`; the working branch is `feat/publish/pipeline`.
- Gradle version is currently a manual `1.0.0` in `gradle.properties` and is expanded into `src/main/resources/fabric.mod.json`.
- The build config produces both the distributable mod JAR and a sources JAR. CI exists at `.github/workflows/build.yml`; it validates the Gradle wrapper, builds/tests on PRs and pushes to `main`, and uses JDK 25.
- Recent commits use PR-linked conventional-style subjects, but their displayed messages have no body; the release workflow will need the final release commit's body to contain the intended release notes.
- `Kira-NT/mc-publish@v3` can publish the same built JAR to Modrinth, CurseForge, and a GitHub Release in one action. It requires Modrinth and CurseForge project IDs plus their respective API-token repository secrets; GitHub Releases require `contents: write`.
- `mc-publish` supports platform-specific artifact lists: publish only the primary JAR to Modrinth/CurseForge, while attaching that JAR plus a generated SHA-256 checksum (`.sha256`) to the GitHub Release.
- The desired release notes are the body of the latest squash-merge commit on `main`; release PRs must therefore retain their PR description as that commit body.

## Approach
- Add a manually dispatched release workflow that always checks out `main`, validates/builds the mod using the existing CI setup, derives the release version from `gradle.properties`, uses the current `main` commit message body as the release notes, generates a SHA-256 checksum, and invokes `mc-publish` once to publish the primary mod JAR to all three destinations while attaching the checksum only to GitHub.
- Use manually bumped `gradle.properties` versions and `v<version>` Git tags; do not release automatically on merges. Treat versions ending in a beta suffix (for example, `1.1.0-beta.1`) as beta/pre-release builds and all other versions as stable releases.
- Before building or publishing, fail if the derived tag already exists, preventing accidental duplicate releases.
- **Manual prerequisite gate — pause implementation here:** create the Modrinth and CurseForge project pages, record their numeric/string project IDs, create API tokens with upload permission, and add them as GitHub repository secrets. Resume only after those values are available.

## Files to modify
- `.github/workflows/release.yml`: new manually dispatched publication workflow.
- `README.md`: document the release prerequisites and maintainer release procedure (only if the project needs this operational guidance).

## Reuse
- `build.gradle`: existing Loom build and artifact generation.
- `gradle.properties`: single source for mod version.
- `src/main/resources/fabric.mod.json`: version placeholder populated by Gradle.

## Steps
- [x] Confirm the desired release and publication behavior.
- [x] Research the selected Fabric distribution platform(s) and GitHub release-action approach.
- [x] Define the version source and release-note format.
- [x] **Manual prerequisite gate:** create Modrinth and CurseForge project pages; configure their IDs and upload tokens as GitHub repository secrets.
- [x] Add the manually dispatched workflow: checkout `main`, derive `v<version>`, fail on an existing tag, build/test with JDK 25, and read the latest commit body.
- [x] Generate a SHA-256 sidecar for the distributable JAR; configure `mc-publish` to upload the JAR to Modrinth/CurseForge and the JAR plus checksum to GitHub Releases.
- [x] Set the release type from the version: `*-beta*` becomes a beta/pre-release; all other versions are stable. Create the immutable tag and GitHub Release with the latest commit body as its notes.

## Verification
- [x] Validate workflow YAML and Gradle build (`ruby` YAML parse, release shell syntax check, `./gradlew build`, and checksum verification).
- [ ] On a release PR merged via squash to `main`, confirm the resulting commit body is the intended notes.
- [ ] Manually dispatch the workflow and verify the GitHub Release has the version tag, primary JAR, SHA-256 file, and commit-body notes; verify the same JAR appears on Modrinth and CurseForge.
