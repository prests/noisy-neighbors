package com.github.prests.noisyneighbors.client.zone;

import java.util.Map;
import java.util.UUID;

public record Zone(UUID id, String name, boolean enabled, Integer color, String dimension, int minX, int minY, int minZ,
                   int maxX, int maxY, int maxZ, Map<String, Integer> volumes) {
  public Zone(UUID id, String name, boolean enabled, String dimension, int minX, int minY, int minZ,
              int maxX, int maxY, int maxZ, Map<String, Integer> volumes) {
    this(id, name, enabled, null, dimension, minX, minY, minZ, maxX, maxY, maxZ, volumes);
  }

  public Zone {
    int lowX = Math.min(minX, maxX), highX = Math.max(minX, maxX);
    int lowY = Math.min(minY, maxY), highY = Math.max(minY, maxY);
    int lowZ = Math.min(minZ, maxZ), highZ = Math.max(minZ, maxZ);
    minX = lowX; maxX = highX; minY = lowY; maxY = highY; minZ = lowZ; maxZ = highZ;
    name = name == null || name.isBlank() ? "Zone" : name.trim();
    color = color == null ? 0x33E5FF : color & 0xFFFFFF;
    volumes = volumes == null ? Map.of() : Map.copyOf(volumes);
  }

  public boolean contains(double x, double y, double z) {
    return Math.floor(x) >= minX && Math.floor(x) <= maxX
        && Math.floor(y) >= minY && Math.floor(y) <= maxY
        && Math.floor(z) >= minZ && Math.floor(z) <= maxZ;
  }

  public double multiplier(String mobId) {
    return enabled ? volumes.getOrDefault(mobId, 100) / 100.0 : 1.0;
  }
}
