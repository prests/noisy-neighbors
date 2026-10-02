package com.github.prests.noisyneighbors.client.sound;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MobSoundCatalogTest {
  @BeforeAll static void loadCatalog() { MobSoundCatalog.load(); }

  @Test void mapsKnownEventsAndRejectsMismatchedSources() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.cow_moody.hurt", "minecraft:cow"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.cow.ambient", "minecraft:pig"));
    assertNull(MobSoundCatalog.mobFor("minecraft:block.note_block.harp", null));
  }

  @Test void sharedEventsRequireTheirSourceMob() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.generic.swim", "minecraft:cow"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.generic.swim", null));
  }
}
