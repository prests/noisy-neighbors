package com.github.prests.noisyneighbors.client.zone;

import java.util.Map;
import java.util.UUID;

public record Zone(UUID id, String name, boolean enabled, Integer color, String dimension, int minX, int minY, int minZ,
                   int maxX, int maxY, int maxZ, Map<String, Integer> volumes,
                   Map<String, Map<String, Integer>> events) {
  public Zone(UUID id, String name, boolean enabled, String dimension, int minX, int minY, int minZ,
              int maxX, int maxY, int maxZ, Map<String, Integer> volumes) {
    this(id, name, enabled, null, dimension, minX, minY, minZ, maxX, maxY, maxZ, volumes, Map.of());
  }

  public Zone(UUID id, String name, boolean enabled, String dimension, int minX, int minY, int minZ,
              int maxX, int maxY, int maxZ, Map<String, Integer> volumes, Map<String, Map<String, Integer>> events) {
    this(id, name, enabled, null, dimension, minX, minY, minZ, maxX, maxY, maxZ, volumes, events);
  }

  /** Compatibility constructor for existing callers and version-1 settings. */
  public Zone(UUID id, String name, boolean enabled, Integer color, String dimension, int minX, int minY, int minZ,
              int maxX, int maxY, int maxZ, Map<String, Integer> volumes) {
    this(id, name, enabled, color, dimension, minX, minY, minZ, maxX, maxY, maxZ, volumes, Map.of());
  }

  public Zone {
    int lowX = Math.min(minX, maxX), highX = Math.max(minX, maxX);
    int lowY = Math.min(minY, maxY), highY = Math.max(minY, maxY);
    int lowZ = Math.min(minZ, maxZ), highZ = Math.max(minZ, maxZ);
    minX = lowX; maxX = highX; minY = lowY; maxY = highY; minZ = lowZ; maxZ = highZ;
    name = name == null || name.isBlank() ? "Zone" : name.trim();
    color = color == null ? 0x33E5FF : color & 0xFFFFFF;
    volumes = volumes == null ? Map.of() : Map.copyOf(volumes);
    if (events == null) events = Map.of();
    var copiedEvents = new java.util.HashMap<String, Map<String, Integer>>();
    events.forEach((mob, values) -> copiedEvents.put(mob, values == null ? Map.of() : Map.copyOf(values)));
    events = Map.copyOf(copiedEvents);
  }

  public boolean contains(double x, double y, double z) {
    return Math.floor(x) >= minX && Math.floor(x) <= maxX
        && Math.floor(y) >= minY && Math.floor(y) <= maxY
        && Math.floor(z) >= minZ && Math.floor(z) <= maxZ;
  }

  public double multiplier(String mobId) {
    return enabled ? volumes.getOrDefault(mobId, 100) / 100.0 : 1.0;
  }

  public double eventMultiplier(String mobId, String eventId) {
    return enabled ? events.getOrDefault(mobId, Map.of()).getOrDefault(eventId, 100) / 100.0 : 1.0;
  }
}
