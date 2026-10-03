# Entity volume-menu fuzzy search plan

## Context

Add fast entity filtering to both the global advanced volume controls and per-zone volume controls. Queries should match partial words across canonical entities and aliases used by shared controls, so examples such as `zombie`, `villager`, `cave`, and `moo` reveal the expected sliders. This first version intentionally follows Minecraft's native substring behavior rather than typo-tolerant edit-distance matching.

## Approach

- Reuse `MobSoundCatalog` as the single source of canonical IDs, aliases, display labels, and searchable variants.
- Follow Minecraft 26.3's own Game Rules search pattern: a native `EditBox`, an immediate responder that repopulates the existing list and resets scroll to the top, and dependency-free `String.contains` matching. Normalize both names and query terms with `Locale.ROOT` so every search is case-insensitive, including mixed-case input.
- Extend that baseline slightly for this feature: strip the `minecraft:` namespace, normalize `_` as a word separator, split the query on whitespace, and require every non-empty term to occur in the combined canonical/alias names. This makes `zombie vill` match `zombie_villager` and lets `moo`/`cave` find shared controls through `variantsFor`.
- On the global screen, expand the header to place a 200-pixel search field and 100-pixel Edit World Zones button on one 310-pixel row with the existing 10-pixel gap: a 2:1 control-width ratio beneath the title.
- On the zone screen, keep the existing details and two edit-action rows first, then add a full-width 310-pixel search field immediately below them and above entity sliders.
- Keep filtered controls alphabetically ordered; this is the simplest predictable start and avoids relevance-scoring rules.
- Keep the global top row and zone edit/search rows present while filtering, and add a disabled translated `No entities found` row when no sliders match.
- Do not add a dependency or typo tolerance. Use Minecraft's native substring-search conventions and retain alphabetical ordering.

## Files to modify

- `src/main/java/com/github/prests/noisyneighbors/client/sound/MobSoundCatalog.java` — add the shared `matchesSearch` behavior beside existing variant/display-name logic.
- `src/main/java/com/github/prests/noisyneighbors/client/ui/GlobalSoundScreen.java` — global search field and filtered controls.
- `src/main/java/com/github/prests/noisyneighbors/client/ui/ZoneSoundScreen.java` — zone search field and filtered controls.
- `src/test/java/com/github/prests/noisyneighbors/client/sound/MobSoundCatalogTest.java` — focused matcher regression tests.
- `src/main/resources/assets/noisy-neighbors/lang/en_us.json` — translated search hint and empty-state text.

## Reuse

- `MobSoundCatalog.mobs()`, `variantsFor(String)`, and `displayName(String)` in `src/main/java/com/github/prests/noisyneighbors/client/sound/MobSoundCatalog.java`.
- Existing `OptionsSubScreen` lists and `MobSlider` controls in both target screens; use public `OptionsList.replaceEntries(...)` rather than introducing a custom list type.
- Existing native `EditBox` + `setResponder` usage in `src/main/java/com/github/prests/noisyneighbors/client/ui/ZoneColorScreen.java`.
- Existing disabled empty-state row pattern in `src/main/java/com/github/prests/noisyneighbors/client/ui/WorldZonesScreen.java`.
- Minecraft 26.3's `AbstractGameRulesScreen`: `EditBox.SEARCH_HINT_STYLE`, header `LinearLayout`, immediate responder, `clearEntries()`, `setScrollAmount(0)`, `Locale.ROOT`, and `String.contains`. The target 26.3 mapped jar confirms these APIs are available to this project.

## Steps

- [ ] Add `MobSoundCatalog.matchesSearch(mob, query)` and focused unit coverage for an empty query, lower/upper/mixed-case partial canonical names, underscores/spaces, all-term multi-word matching, shared-control aliases (`moo` and `cave`), rejected terms, and confirmation that `villager` does not match `wandering_trader`. Do not add edit-distance typo matching or search-only synonyms.
- [ ] Keep matching results in the existing natural alphabetical order; do not add relevance scoring.
- [ ] In `GlobalSoundScreen`, replace the title-only header with a native vertical header containing the title and a horizontal top row: a narrated 200-pixel search `EditBox`, 10-pixel gap, and 100-pixel Edit World Zones button. Move that button out of the scrolling list, retain its current enabled-state rule, and filter only the slider list via one initial/refresh population method.
- [ ] In `ZoneSoundScreen`, add the narrated search field as a full-width list row after zone details/actions. Retain those fixed entry objects when replacing filtered slider entries so the field remains focused while typing; rebuild sliders from current stored volumes or show the no-results row, ensuring filtering never discards a slider change.
- [ ] On each responder-driven refresh, reset list scroll to the top while leaving keyboard focus and the query intact.
- [ ] Add translated `Search entities` hint/narration and `No entities found` strings.

## Verification

- Run `./gradlew test`.
- Run `./gradlew build` because the change touches Minecraft/Fabric UI code.
- Launch the development client and manually verify both menus with empty, lower/upper/mixed-case, partial, multi-word, alias, and no-result queries; specifically check `zombie`, `ZoMbIe`, `zombie vill`, `villager`, `cave`, and `moo`. Confirm `villager` excludes `wandering_trader`.
- Confirm results remain alphabetized and the no-results row appears instead of a blank list.
- Confirm the global top row stays visible with the search field taking two-thirds of the control width and Edit World Zones taking one-third; confirm the zone search spans the full row directly below all edit options.
- Confirm slider changes persist while filtering and after closing/reopening each screen.
- Keyboard/narration check: focus search, type and edit a query continuously, Tab into filtered sliders, return to search, and confirm the native field hint/name, visible focus, logical order, and result empty state are conveyed. Resize the game window and confirm neither search layout overlaps the list.

## Research basis

- Java 25 [`String.contains`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/String.html) and language-neutral [`Locale.ROOT`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Locale.html) support the small, local, dependency-free matcher.
- Minecraft/NeoForge 26.1 API docs confirm [`EditBox.setResponder`](https://aldak0.ru/javadoc/26.1.x/net/minecraft/client/gui/components/EditBox.html) and inherited list clearing/replacement APIs on [`OptionsList`](https://aldak0.ru/javadoc/26.1.x/net/minecraft/client/gui/components/OptionsList.html); the project's target 26.3 mapped classes were also inspected directly.
- Lucene's [`FuzzyQuery`](https://lucene.apache.org/core/10_5_0/core/org/apache/lucene/search/FuzzyQuery.html) uses Damerau–Levenshtein distance and caps supported edits at two, illustrating that typo tolerance adds distance thresholds and scored ordering. That is unnecessary for the partial-name examples unless typo recovery is explicitly desired.
