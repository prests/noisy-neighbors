package com.github.prests.noisyneighbors.client.config;

import static org.junit.jupiter.api.Assertions.*;

import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.sound.VolumePolicy;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsStoreIntegrationTest {
  @BeforeAll static void loadCatalog() { MobSoundCatalog.load(); }

  @TempDir Path tempDir;

  @Test void localWorldStateSurvivesEditsSoundPolicyAndDeletion() {
    Path file = tempDir.resolve("settings.json");
    WorldIdentity local = WorldIdentity.local("first-save", "First World");
    UUID zoneId = UUID.randomUUID();
    SettingsStore.Store store = SettingsStore.at(file);
    store.data().global.put("minecraft:cow", 80);
    store.addZone(local, new Zone(zoneId, "  Barn  ", true, 0x123456, "minecraft:overworld", 4, 8, 2, -2, 8, -4,
        Map.of("minecraft:cow", 25)));
    store.save();

    SettingsStore.Store reloaded = SettingsStore.at(file);
    reloaded.load();
    Zone zone = reloaded.data().worlds.get(local.key()).zones.getFirst();
    assertEquals("Barn", zone.name());
    assertEquals(-2, zone.minX());
    assertEquals(.2F, VolumePolicy.multiplier(reloaded.data(), "minecraft:entity.cow.ambient", "minecraft:cow", local.key(), "minecraft:overworld", 0, 8, 0));
    assertEquals(.8F, VolumePolicy.multiplier(reloaded.data(), "minecraft:entity.cow.ambient", "minecraft:cow", local.key(), "minecraft:overworld", 0, 9, 0));

    reloaded.removeWorld(local.key());
    SettingsStore.Store afterDelete = SettingsStore.at(file);
    afterDelete.load();
    assertFalse(afterDelete.data().worlds.containsKey(local.key()));
  }

  @Test void serverJoinUsesNormalizedIdentityAndDeletionLeavesOtherWorlds() {
    Path file = tempDir.resolve("settings.json");
    WorldIdentity local = WorldIdentity.local("save", "Local");
    WorldIdentity joined = WorldIdentity.server(" Example.COM:25565 ", "Example");
    SettingsStore.Store store = SettingsStore.at(file);
    store.addZone(local, zone(UUID.randomUUID(), "Local zone", "minecraft:overworld", 100, 0, 0, 0, 0, 0, 0));
    store.addZone(joined, zone(UUID.randomUUID(), "Server zone", "minecraft:overworld", 50, 0, 0, 0, 0, 0, 0));
    store.save();

    SettingsStore.Store rejoined = SettingsStore.at(file);
    rejoined.load();
    String equivalentKey = WorldIdentity.server("example.com:25565", "New label").key();
    assertEquals(joined.key(), equivalentKey);
    assertEquals(.5F, VolumePolicy.multiplier(rejoined.data(), "minecraft:entity.cow.ambient", "minecraft:cow", equivalentKey, "minecraft:overworld", 0, 0, 0));

    rejoined.removeWorld(equivalentKey);
    SettingsStore.Store afterDelete = SettingsStore.at(file);
    afterDelete.load();
    assertFalse(afterDelete.data().worlds.containsKey(joined.key()));
    assertTrue(afterDelete.data().worlds.containsKey(local.key()));
  }

  @Test void zoneEditsAreScopedToTheirIdAndDimension() {
    Path file = tempDir.resolve("settings.json");
    WorldIdentity world = WorldIdentity.local("save", "World");
    UUID editedId = UUID.randomUUID();
    UUID preservedId = UUID.randomUUID();
    SettingsStore.Store store = SettingsStore.at(file);
    store.addZone(world, zone(editedId, "Original", "minecraft:overworld", 50, 0, 0, 0, 1, 1, 1));
    store.addZone(world, zone(preservedId, "Preserved", "minecraft:overworld", 50, 1, 1, 1, 2, 2, 2));
    store.updateZone(world.key(), editedId, zone -> new Zone(zone.id(), "Edited", zone.enabled(), 0xAABBCC, "minecraft:the_nether",
        -2, 3, -4, 4, 5, 6, Map.of("minecraft:cow", 25)));
    store.save();

    SettingsStore.Store reloaded = SettingsStore.at(file);
    reloaded.load();
    Zone edited = reloaded.data().worlds.get(world.key()).zones.stream().filter(zone -> zone.id().equals(editedId)).findFirst().orElseThrow();
    Zone preserved = reloaded.data().worlds.get(world.key()).zones.stream().filter(zone -> zone.id().equals(preservedId)).findFirst().orElseThrow();
    assertEquals("Edited", edited.name());
    assertEquals(0xAABBCC, edited.color());
    assertEquals("minecraft:the_nether", edited.dimension());
    assertEquals("Preserved", preserved.name());
    assertEquals(.25F, VolumePolicy.multiplier(reloaded.data(), "minecraft:entity.cow.ambient", "minecraft:cow", world.key(), "minecraft:the_nether", 0, 3, 0));
    assertEquals(.5F, VolumePolicy.multiplier(reloaded.data(), "minecraft:entity.cow.ambient", "minecraft:cow", world.key(), "minecraft:overworld", 1, 1, 1));

    reloaded.removeZone(world.key(), editedId);
    reloaded.save();
    assertEquals(1, reloaded.data().worlds.get(world.key()).zones.size());
    assertEquals(preservedId, reloaded.data().worlds.get(world.key()).zones.getFirst().id());
  }

  @Test void ignoresMissingCorruptUnsupportedAndInvalidPersistedData() throws Exception {
    Path file = tempDir.resolve("settings.json");
    SettingsStore.Store missing = SettingsStore.at(file);
    missing.load();
    assertTrue(missing.data().worlds.isEmpty());

    missing.data().global.put("minecraft:cow", 50);
    Files.writeString(file, "not json");
    missing.load();
    assertEquals(50, missing.data().global.get("minecraft:cow"));

    Files.writeString(file, "{\"version\":2}");
    SettingsStore.Store unsupported = SettingsStore.at(file);
    unsupported.load();
    assertTrue(unsupported.data().global.isEmpty());

    Files.writeString(file, "{\"version\":1,\"global\":{\"minecraft:cow\":101,\"\":50},\"worlds\":{}}");
    SettingsStore.Store invalid = SettingsStore.at(file);
    invalid.load();
    assertTrue(invalid.data().global.isEmpty());
  }

  private static Zone zone(UUID id, String name, String dimension, int volume, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    return new Zone(id, name, true, dimension, minX, minY, minZ, maxX, maxY, maxZ, new HashMap<>(Map.of("minecraft:cow", volume)));
  }
}
