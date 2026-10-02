package com.github.prests.noisyneighbors.client.sound;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Explicit data only: do not infer mobs from an event-name prefix. */
public final class MobSoundCatalog {
  private static Map<String, String> eventToMob = Map.of();
  private static Set<String> sharedEvents = Set.of();

  private MobSoundCatalog() {}
  public static void load() {
    try (var stream = MobSoundCatalog.class.getResourceAsStream("/assets/noisy-neighbors/mob_sounds.json")) {
      if (stream == null) throw new IllegalStateException("Missing mob sound catalog");
      Catalog catalog = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Catalog.class);
      Map<String, String> reverse = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : catalog.mobs.entrySet()) for (String event : entry.getValue()) {
        if (reverse.put(event, entry.getKey()) != null) throw new IllegalStateException("Ambiguous event " + event);
      }
      eventToMob = Collections.unmodifiableMap(reverse);
      sharedEvents = Collections.unmodifiableSet(new HashSet<>(catalog.shared));
    } catch (Exception exception) {
      NoisyNeighbors.LOGGER.error("Noisy Neighbor sound catalog is invalid; controls are disabled", exception);
      eventToMob = Map.of(); sharedEvents = Set.of();
    }
  }
  public static String mobFor(String eventId, String sourceMobId) {
    if (sharedEvents.contains(eventId)) return sourceMobId;
    String mob = eventToMob.get(eventId);
    return sourceMobId == null || sourceMobId.equals(mob) ? mob : null;
  }
  public static boolean isKnown(String eventId) { return eventToMob.containsKey(eventId) || sharedEvents.contains(eventId); }
  public static Set<String> mobs() { return Set.copyOf(eventToMob.values()); }
  private static final class Catalog { Map<String, List<String>> mobs = Map.of(); List<String> shared = List.of(); }
}
