package com.github.prests.noisyneighbors.client.sound;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Explicit data only: do not infer mobs from an event-name prefix. */
public final class MobSoundCatalog {
  private static Map<String, Set<String>> eventToMobs = Map.of();
  private static Map<String, String> sourceAliases = Map.of();
  private static Set<String> mobIds = Set.of();
  private static Map<String, List<String>> mobVariants = Map.of();
  private static Set<String> sharedEvents = Set.of();

  private MobSoundCatalog() {}
  public static void load() {
    try (var stream = MobSoundCatalog.class.getResourceAsStream("/assets/noisy-neighbors/mob_sounds.json")) {
      if (stream == null) throw new IllegalStateException("Missing mob sound catalog");
      Catalog catalog = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Catalog.class);
      Map<String, Set<String>> reverse = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : catalog.mobs.entrySet()) for (String event : entry.getValue()) {
        reverse.computeIfAbsent(event, ignored -> new LinkedHashSet<>()).add(entry.getKey());
      }
      reverse.replaceAll((event, mobs) -> Collections.unmodifiableSet(mobs));
      eventToMobs = Collections.unmodifiableMap(reverse);
      sourceAliases = Map.copyOf(catalog.aliases);
      mobIds = Set.copyOf(catalog.mobs.keySet());
      Map<String, List<String>> variants = new HashMap<>();
      for (String mob : mobIds) variants.put(mob, new ArrayList<>(List.of(mob)));
      for (Map.Entry<String, String> alias : sourceAliases.entrySet()) {
        variants.computeIfAbsent(alias.getValue(), ignored -> new ArrayList<>()).add(alias.getKey());
      }
      variants.replaceAll((mob, aliases) -> List.copyOf(aliases));
      mobVariants = Map.copyOf(variants);
      sharedEvents = Collections.unmodifiableSet(new HashSet<>(catalog.shared));
    } catch (Exception exception) {
      NoisyNeighbors.LOGGER.error("Noisy Neighbor sound catalog is invalid; controls are disabled", exception);
      eventToMobs = Map.of(); sourceAliases = Map.of(); mobIds = Set.of(); mobVariants = Map.of(); sharedEvents = Set.of();
    }
  }
  public static String mobFor(String eventId, String sourceMobId) {
    String mob = sourceMobId == null ? null : sourceAliases.getOrDefault(sourceMobId, sourceMobId);
    if (sharedEvents.contains(eventId)) return mob;
    Set<String> mobs = eventToMobs.get(eventId);
    if (mobs == null) return null;
    if (mob != null) return mobs.contains(mob) ? mob : null;
    return mobs.iterator().next();
  }
  public static String mobForSource(String sourceMobId) {
    String mob = sourceAliases.getOrDefault(sourceMobId, sourceMobId);
    return mobIds.contains(mob) ? mob : null;
  }
  public static boolean isKnown(String eventId) { return eventToMobs.containsKey(eventId) || sharedEvents.contains(eventId); }
  public static Set<String> mobs() { return mobIds; }
  public static List<String> variantsFor(String mob) { return mobVariants.getOrDefault(mob, List.of(mob)); }
  public static String displayName(String mob) {
    return String.join("/", variantsFor(mob).stream().map(id -> id.replace("minecraft:", "")).toList());
  }
  private static final class Catalog {
    Map<String, List<String>> mobs = Map.of();
    Map<String, String> aliases = Map.of();
    List<String> shared = List.of();
  }
}
