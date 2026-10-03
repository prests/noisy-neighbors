# Fine-grained entity sound volume controls

## Context

Add per-sound-event volume controls beneath the existing per-entity global and zone controls. The effective sound level should remain Minecraft's calculated volume multiplied by the configured layers:

`Minecraft volume × global entity × global sound event × ∏(each matching zone entity × that zone sound event)`

Keep the settings file at version `1`; existing files without fine-grained maps must continue to load, with absent fine-grained values behaving as `100%`. No migration of existing separate llama/trader-llama values is required.

Investigation of the reported cow-fall reproduction found an existing limitation: normal `Entity.playSound(...)` calls send only event ID plus coordinates (`ClientboundSoundPacket`), so generic fall/hurt/death events usually arrive without an entity. Minecraft includes an entity ID only for the much less common `Level.playSound(..., sourceEntity, ...)` overload (`ClientboundSoundEntityPacket` / `EntityBoundSoundInstance`). Unique catalog events can still resolve their entity group from the event ID. The six generic IDs cannot. Source inspection also confirms `TraderLlama` extends `Llama` without overriding its sounds, so both currently duplicated parent controls use the same nine `minecraft:entity.llama.*` IDs.

## Approach

- Keep the existing entity sliders as the parent multipliers.
- Add a compact gear button beside each entity slider. Its tooltip will read **Advanced Controls**, and it will open a separate scrollable fine-grained screen for that entity group.
- List every raw event assigned to the canonical entity in `mob_sounds.json` using its exact namespace ID as the slider label (for example, `minecraft:entity.cow.hurt`). Do not merge similar IDs such as `minecraft:entity.cow.hurt` and `minecraft:entity.cow_moody.hurt`; cows and mooshrooms still share the canonical `minecraft:cow` parent group.
- Store fine-grained settings as nested canonical-entity/event maps in both global data and each `Zone`, sharing values among aliases in one canonical group.
- Offer fine-grained sliders only for event IDs that resolve directly to one canonical parent group without entity metadata. Exclude the six generic IDs and do not add positional guessing.
- Canonicalize `minecraft:trader_llama` to `minecraft:llama` exactly like the existing cow/mooshroom and spider/cave-spider groups: remove the duplicate parent entry, add a source alias, and show one combined `llama/trader_llama` control. Reuse `MobIcons.draw(...)` so the existing llama portrait supplies the left half and trader-llama portrait supplies the right half. Their nine exact IDs then become directly controllable for the shared group; do not migrate prior separate settings.
- Keep fine-grained settings sparse: missing or `100%` means no extra attenuation, returning a slider to `100%` removes its event entry, and an emptied entity map is also removed.
- Reuse the existing mob sound catalog as the source of entity/event relationships and the existing options-list/slider UI patterns.
- Preserve the current save-on-screen-close behavior.

## Files to modify

Critical files identified by tracing the catalog → policy → mixin path and every `Zone` reconstruction site:

- `src/main/java/com/github/prests/noisyneighbors/client/sound/MobSoundCatalog.java`
- `src/main/java/com/github/prests/noisyneighbors/client/sound/VolumePolicy.java`
- `src/main/java/com/github/prests/noisyneighbors/client/config/SettingsStore.java`
- `src/main/java/com/github/prests/noisyneighbors/client/zone/Zone.java`
- `src/main/java/com/github/prests/noisyneighbors/client/ui/GlobalSoundScreen.java`
- `src/main/java/com/github/prests/noisyneighbors/client/ui/ZoneSoundScreen.java`
- New `src/main/java/com/github/prests/noisyneighbors/client/ui/FineGrainedSoundScreen.java`
- `src/main/java/com/github/prests/noisyneighbors/client/ui/ZoneNameScreen.java`
- `src/main/java/com/github/prests/noisyneighbors/client/ui/ZoneColorScreen.java`
- `src/main/java/com/github/prests/noisyneighbors/client/zone/ZoneSelection.java`
- `src/main/resources/assets/noisy-neighbors/lang/en_us.json`
- `src/main/resources/assets/noisy-neighbors/mob_sounds.json`
- `src/test/java/com/github/prests/noisyneighbors/client/sound/MobSoundCatalogTest.java`
- `src/test/java/com/github/prests/noisyneighbors/client/sound/VolumePolicyTest.java`
- `src/test/java/com/github/prests/noisyneighbors/client/config/SettingsStoreIntegrationTest.java`
- `src/test/java/com/github/prests/noisyneighbors/client/zone/ZoneTest.java`
- `src/gametest/java/com/github/prests/noisyneighbors/gametest/NoisyNeighborsClientGameTest.java`
- `README.md`

## Reuse

- Catalog mappings and source disambiguation: `MobSoundCatalog`
- Central multiplier composition: `VolumePolicy.multiplier(...)`
- Sparse percentage validation and atomic save: `SettingsStore`
- Existing scrollable/searchable Minecraft option screens: `GlobalSoundScreen` and `ZoneSoundScreen`
- Existing immutable zone update path: `SettingsStore.updateZone(...)`
- Existing alias-driven split portrait rendering in `MobIcons.draw(...)`, already used for cow/mooshroom and spider/cave-spider; adding the trader-llama alias automatically produces llama-left/trader-llama-right
- Minecraft's native `Button` tooltip and narration support for an accessible `⚙` control; no new icon asset or dependency
- Existing `SoundEngineMixin` integration remains unchanged because all new multiplication stays behind `VolumePolicy.multiplier(...)`

## Steps

- [ ] In `mob_sounds.json`, remove the duplicate trader-llama parent entry and add `minecraft:trader_llama → minecraft:llama` to `aliases`; verify display/search and split-icon ordering are `llama/trader_llama`, with llama on the left.
- [ ] Expose each canonical entity group's directly controllable raw event IDs from `MobSoundCatalog` in catalog order: include only IDs whose reverse mapping resolves to that one canonical group and exclude generic/source-dependent IDs without merging or renaming events.
- [ ] Extend `SettingsStore.Data` and `Zone` with nested `Map<canonical entity ID, Map<event ID, percentage>>` fields while retaining `version: 1`; normalize missing maps, reject invalid IDs/percentages, and omit defaulted entries.
- [ ] Add one `FineGrainedSoundScreen` with global and zone constructors. It will show the canonical entity group in the title, populate a plain scrollable options list, update the correct sparse map, save on close, and return to its parent.
- [ ] Change global and zone entity rows to fit the existing mob icon, parent slider, and a keyboard-focusable `⚙` button with an **Advanced Controls** tooltip while preserving parent-list scrolling and search.
- [ ] Multiply global entity and event percentages once, then both percentages from every enabled matching zone in `VolumePolicy`; leave `SoundEngineMixin` unchanged so Minecraft's already-calculated native volume is still multiplied exactly once.
- [ ] Carry the new zone map through every immutable reconstruction in `ZoneSoundScreen`, `ZoneNameScreen`, `ZoneColorScreen`, and `ZoneSelection`; new zones start with an empty map.
- [ ] Extend catalog tests for exact event exposure, llama alias grouping/order, and exclusion of generic IDs; extend policy tests for the complete formula, unrelated-event defaults, disabled/foreign zones, and overlapping-zone stacking.
- [ ] Extend persistence/zone tests for version-1 backward compatibility, sparse nested-map round trips, validation, and preservation through zone edits.
- [ ] Extend the client GameTest to exercise both gear navigation paths and verify that the fine-grained screen renders exact event-ID sliders, while retaining current parent search coverage.
- [ ] Update `README.md` with global and zone advanced-control navigation and multiplicative behavior.

## Verification

- Run `./gradlew test`.
- Run `./gradlew build`.
- Run `./gradlew runProductionClientGameTest` because the parent rows and nested screen navigation change.
- Manually run `./gradlew runClient` and verify global and zone navigation, mouse/keyboard controls, persistence, and audible composition (for example: cow entity at 50%, cow hurt at 0%, zone entity at 50%, cow hurt zone override as configured).
- Load an existing version-1 settings file without fine-grained fields and confirm default `100%` fine-grained behavior and successful subsequent save; no trader-llama setting migration is expected.
- Inspect the resulting JSON to confirm `100%` event settings and empty per-entity event maps are absent.

## Decisions

- Show each directly controllable raw event separately using its exact namespace ID.
- Canonical aliases share settings, and all matching zones stack multiplicatively.
- Exclude generic events because most instances carry coordinates but no entity ID; add no heuristic attribution.
- Group llama/trader llama into one canonical parent and split its portrait llama-left/trader-llama-right, matching existing alias groups; add no settings migration.
- Omit `100%` fine-grained values from JSON.
- Use a gear button with an **Advanced Controls** tooltip; the nested page is scroll-only, without search.
