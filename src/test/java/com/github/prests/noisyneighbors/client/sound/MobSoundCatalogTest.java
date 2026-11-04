package com.github.prests.noisyneighbors.client.sound;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MobSoundCatalogTest {
  @BeforeAll static void loadCatalog() { MobSoundCatalog.load(); }

  @Test void retainsTheMinecraft1211CatalogAndAliases() {
    assertEquals(78, MobSoundCatalog.mobs().size());
    assertEquals(602, MobSoundCatalog.eventCount());
    assertTrue(MobSoundCatalog.mobs().contains("minecraft:cow"));
    assertFalse(MobSoundCatalog.mobs().contains("minecraft:creaking"));
    assertFalse(MobSoundCatalog.mobs().contains("minecraft:copper_golem"));
    assertEquals("minecraft:cow", MobSoundCatalog.mobForSource("minecraft:mooshroom"));
    assertEquals("minecraft:spider", MobSoundCatalog.mobForSource("minecraft:cave_spider"));
    assertEquals("minecraft:llama", MobSoundCatalog.mobForSource("minecraft:trader_llama"));
  }

  @Test void mapsSupportedEventsAndExcludesNewerOnes() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.cow.ambient", "minecraft:cow"));
    assertEquals("minecraft:goat", MobSoundCatalog.mobFor("minecraft:entity.goat.screaming.horn_break", "minecraft:goat"));
    assertEquals("minecraft:parrot", MobSoundCatalog.mobFor("minecraft:entity.parrot.imitate.zombie", "minecraft:parrot"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.cow_moody.hurt", "minecraft:cow"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.pig_mini.eat", "minecraft:pig"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.parrot.imitate.breeze", "minecraft:parrot"));
  }

  @Test void sharedEventsRequireARecognizedSource() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.generic.swim", "minecraft:cow"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.generic.swim", null));
    assertNull(MobSoundCatalog.mobFor("minecraft:block.note_block.harp", null));
  }

  @Test void supportsSearchAndFineGrainedControls() {
    assertTrue(MobSoundCatalog.matchesSearch("minecraft:cow", "moo"));
    assertTrue(MobSoundCatalog.matchesSearch("minecraft:spider", "cave"));
    assertFalse(MobSoundCatalog.matchesSearch("minecraft:zombie_villager", "skeleton"));
    assertTrue(MobSoundCatalog.controllableEventsFor("minecraft:goat")
        .contains("minecraft:entity.goat.screaming.horn_break"));
    assertFalse(MobSoundCatalog.controllableEventsFor("minecraft:cow").contains("minecraft:entity.generic.hurt"));
  }
}
