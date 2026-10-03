package com.github.prests.noisyneighbors.client.zone;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ZoneTest {
  @Test void normalizesBoundsAndSanitizesDisplayFields() {
    Zone zone = new Zone(UUID.randomUUID(), "  ", true, -1, "minecraft:overworld", 4, 8, 2, -2, 8, -4, null);

    assertEquals("Zone", zone.name());
    assertEquals(0xFFFFFF, zone.color());
    assertTrue(zone.contains(-2, 8.9, -4));
    assertTrue(zone.contains(4.99, 8, 2.99));
    assertFalse(zone.contains(0, 9, 0));
  }

  @Test void onlyEnabledZonesApplyConfiguredVolume() {
    Zone enabled = new Zone(UUID.randomUUID(), "Quiet", true, "minecraft:overworld", 0, 0, 0, 1, 1, 1, Map.of("minecraft:cow", 25));
    Zone disabled = new Zone(UUID.randomUUID(), "Off", false, "minecraft:overworld", 0, 0, 0, 1, 1, 1, Map.of("minecraft:cow", 0));

    assertEquals(.25, enabled.multiplier("minecraft:cow"));
    assertEquals(1, enabled.multiplier("minecraft:pig"));
    assertEquals(1, disabled.multiplier("minecraft:cow"));
  }

  @Test void appliesSparseEventVolumeOnlyWhenEnabled() {
    Zone zone = new Zone(UUID.randomUUID(), "Quiet", true, "minecraft:overworld", 0, 0, 0, 1, 1, 1, Map.of(),
        Map.of("minecraft:cow", Map.of("minecraft:entity.cow.hurt", 25)));

    assertEquals(.25, zone.eventMultiplier("minecraft:cow", "minecraft:entity.cow.hurt"));
    assertEquals(1, zone.eventMultiplier("minecraft:cow", "minecraft:entity.cow.ambient"));
  }
}
