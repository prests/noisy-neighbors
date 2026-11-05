package com.github.prests.noisyneighbors.client.sound;

import static org.junit.jupiter.api.Assertions.*;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VolumePolicyTest {
  @BeforeAll static void loadCatalog() { MobSoundCatalog.load(); }

  @Test void combinesGlobalAndOverlappingZoneVolumesAtSoundPosition() {
    SettingsStore.Data data = new SettingsStore.Data();
    data.global.put("minecraft:cow", 50);
    SettingsStore.World world = new SettingsStore.World();
    world.zones.add(zone("minecraft:overworld", true, 50, 0, 0, 0, 2, 2, 2));
    world.zones.add(zone("minecraft:overworld", true, 50, 1, 1, 1, 3, 3, 3));
    data.worlds.put("local:test", world);

    assertEquals(.125F, VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", "minecraft:cow", "local:test", "minecraft:overworld", 1, 1, 1));
    assertEquals(.125F, VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", null, "local:test", "minecraft:overworld", 1, 1, 1));
    assertEquals(.25F, VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", "minecraft:cow", "local:test", "minecraft:overworld", 0, 0, 0));
    assertEquals(1F, VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", "minecraft:pig", "local:test", "minecraft:overworld", 1, 1, 1));
    assertEquals(1F, VolumePolicy.multiplier(data, "minecraft:block.note_block.harp", null, "local:test", "minecraft:overworld", 1, 1, 1));
  }

  @Test void multipliesGlobalAndZoneEventControlsOnlyForTheMatchingEvent() {
    SettingsStore.Data data = new SettingsStore.Data();
    data.global.put("minecraft:cow", 50);
    data.events.put("minecraft:cow", Map.of("minecraft:entity.cow.hurt", 80));
    SettingsStore.World world = new SettingsStore.World();
    world.zones.add(new Zone(UUID.randomUUID(), "Zone", true, "minecraft:overworld", 0, 0, 0, 1, 1, 1,
        Map.of("minecraft:cow", 50), Map.of("minecraft:cow", Map.of("minecraft:entity.cow.hurt", 25))));
    data.worlds.put("local:test", world);

    assertEquals(.05F, VolumePolicy.multiplier(data, "minecraft:entity.cow.hurt", "minecraft:cow", "local:test", "minecraft:overworld", 0, 0, 0));
    assertEquals(.25F, VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", "minecraft:cow", "local:test", "minecraft:overworld", 0, 0, 0));
  }

  @Test void multipliesGlobalAndEnabledZoneChattinessBeforeOneRoll() {
    SettingsStore.Data data = new SettingsStore.Data();
    data.chattiness.put("minecraft:cow", 50);
    SettingsStore.World world = new SettingsStore.World();
    world.zones.add(chattyZone("minecraft:overworld", true, 50));
    world.zones.add(chattyZone("minecraft:overworld", true, 50));
    world.zones.add(chattyZone("minecraft:overworld", false, 0));
    world.zones.add(chattyZone("minecraft:the_nether", true, 0));
    data.worlds.put("local:test", world);

    double chattiness = VolumePolicy.chattiness(data, "minecraft:entity.cow.ambient", "minecraft:cow", "local:test",
        "minecraft:overworld", 0, 0, 0);
    assertEquals(.125, chattiness);
    assertTrue(VolumePolicy.passesChattiness(chattiness, .124));
    assertFalse(VolumePolicy.passesChattiness(chattiness, .125));
    assertFalse(VolumePolicy.passesChattiness(0, 0));
    assertTrue(VolumePolicy.passesChattiness(1, .999));
  }

  @Test void ignoresDisabledOrForeignZonesAndAppliesTheOriginalVolumeOnce() {
    SettingsStore.Data data = new SettingsStore.Data();
    SettingsStore.World world = new SettingsStore.World();
    world.zones.add(zone("minecraft:the_nether", true, 0, 0, 0, 0, 0, 0, 0));
    world.zones.add(zone("minecraft:overworld", false, 0, 0, 0, 0, 0, 0, 0));
    data.worlds.put("local:test", world);

    float multiplier = VolumePolicy.multiplier(data, "minecraft:entity.cow.ambient", "minecraft:cow", "local:test", "minecraft:overworld", 0, 0, 0);
    assertEquals(1F, multiplier);
    assertEquals(.4F, VolumePolicy.apply(.4F, multiplier));
    assertEquals(0F, VolumePolicy.apply(.4F, 0F));
  }

  private static Zone zone(String dimension, boolean enabled, int volume, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    return new Zone(UUID.randomUUID(), "Zone", enabled, dimension, minX, minY, minZ, maxX, maxY, maxZ, Map.of("minecraft:cow", volume));
  }

  private static Zone chattyZone(String dimension, boolean enabled, int chattiness) {
    return new Zone(UUID.randomUUID(), "Zone", enabled, null, dimension, 0, 0, 0, 1, 1, 1, Map.of(), Map.of(),
        Map.of("minecraft:cow", chattiness));
  }
}
