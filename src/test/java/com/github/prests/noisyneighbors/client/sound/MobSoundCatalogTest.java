package com.github.prests.noisyneighbors.client.sound;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MobSoundCatalogTest {
  @BeforeAll static void loadCatalog() { MobSoundCatalog.load(); }

  @Test void mapsVariantEventsAndRejectsMismatchedSources() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.cow_moody.hurt", "minecraft:cow"));
    assertEquals("minecraft:pig", MobSoundCatalog.mobFor("minecraft:entity.pig_mini.eat", "minecraft:pig"));
    assertEquals("minecraft:parrot", MobSoundCatalog.mobFor("minecraft:entity.parrot.imitate.zombie", "minecraft:parrot"));
    assertEquals("minecraft:armadillo", MobSoundCatalog.mobFor("minecraft:entity.armadillo.hurt_reduced", "minecraft:armadillo"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.cow.ambient", "minecraft:pig"));
    assertNull(MobSoundCatalog.mobFor("minecraft:block.note_block.harp", null));
  }

  @Test void mapsRequestedAnimalAndGolemControls() {
    Map.ofEntries(
        Map.entry("minecraft:bat", "minecraft:entity.bat.takeoff"),
        Map.entry("minecraft:bee", "minecraft:entity.bee.loop_aggressive"),
        Map.entry("minecraft:fox", "minecraft:entity.fox.teleport"),
        Map.entry("minecraft:goat", "minecraft:entity.goat.screaming.ram_impact"),
        Map.entry("minecraft:llama", "minecraft:entity.llama.spit"),
        Map.entry("minecraft:ocelot", "minecraft:entity.ocelot.ambient"),
        Map.entry("minecraft:panda", "minecraft:entity.panda.sneeze"),
        Map.entry("minecraft:rabbit", "minecraft:entity.rabbit.jump"),
        Map.entry("minecraft:axolotl", "minecraft:entity.axolotl.idle_water"),
        Map.entry("minecraft:turtle", "minecraft:entity.turtle.shamble_baby"),
        Map.entry("minecraft:allay", "minecraft:entity.allay.item_thrown"),
        Map.entry("minecraft:sniffer", "minecraft:entity.sniffer.digging"),
        Map.entry("minecraft:sulfur_cube", "minecraft:entity.small_sulfur_cube.squish"),
        Map.entry("minecraft:copper_golem", "minecraft:entity.copper_golem_oxidized.spin"),
        Map.entry("minecraft:iron_golem", "minecraft:entity.iron_golem.repair"),
        Map.entry("minecraft:snow_golem", "minecraft:entity.snow_golem.shear"),
        Map.entry("minecraft:villager", "minecraft:entity.villager.work_librarian")
    ).forEach((mob, event) -> assertEquals(mob, MobSoundCatalog.mobFor(event, mob)));
  }

  @Test void mapsRequestedHostileControls() {
    Map.ofEntries(
        Map.entry("minecraft:bogged", "minecraft:entity.bogged.shear"),
        Map.entry("minecraft:camel_husk", "minecraft:entity.camel_husk.dash_ready"),
        Map.entry("minecraft:drowned", "minecraft:entity.drowned.ambient_water"),
        Map.entry("minecraft:husk", "minecraft:entity.husk.step"),
        Map.entry("minecraft:parched", "minecraft:entity.parched.ambient"),
        Map.entry("minecraft:skeleton", "minecraft:entity.skeleton.shoot"),
        Map.entry("minecraft:skeleton_horse", "minecraft:entity.skeleton_horse.jump_water"),
        Map.entry("minecraft:stray", "minecraft:entity.stray.ambient"),
        Map.entry("minecraft:zombie", "minecraft:entity.zombie.attack_iron_door"),
        Map.entry("minecraft:zombie_horse", "minecraft:entity.zombie_horse.angry"),
        Map.entry("minecraft:zombie_nautilus", "minecraft:entity.zombie_nautilus.dash_ready_land"),
        Map.entry("minecraft:zombie_villager", "minecraft:entity.zombie_villager.cure"),
        Map.entry("minecraft:spider", "minecraft:entity.spider.step"),
        Map.entry("minecraft:breeze", "minecraft:entity.breeze.whirl"),
        Map.entry("minecraft:creaking", "minecraft:entity.creaking.unfreeze"),
        Map.entry("minecraft:creeper", "minecraft:entity.creeper.primed"),
        Map.entry("minecraft:elder_guardian", "minecraft:entity.elder_guardian.ambient_land"),
        Map.entry("minecraft:guardian", "minecraft:entity.guardian.flop"),
        Map.entry("minecraft:phantom", "minecraft:entity.phantom.swoop"),
        Map.entry("minecraft:silverfish", "minecraft:entity.silverfish.step"),
        Map.entry("minecraft:slime", "minecraft:entity.slime.squish_small"),
        Map.entry("minecraft:warden", "minecraft:entity.warden.sonic_boom"),
        Map.entry("minecraft:witch", "minecraft:entity.witch.throw"),
        Map.entry("minecraft:evoker", "minecraft:entity.evoker.prepare_wololo"),
        Map.entry("minecraft:pillager", "minecraft:entity.pillager.celebrate"),
        Map.entry("minecraft:ravager", "minecraft:entity.ravager.stunned"),
        Map.entry("minecraft:vex", "minecraft:entity.vex.charge"),
        Map.entry("minecraft:vindicator", "minecraft:entity.vindicator.celebrate"),
        Map.entry("minecraft:blaze", "minecraft:entity.blaze.burn"),
        Map.entry("minecraft:ghast", "minecraft:entity.ghast.warn"),
        Map.entry("minecraft:happy_ghast", "minecraft:entity.ghastling.spawn"),
        Map.entry("minecraft:hoglin", "minecraft:entity.hoglin.converted_to_zombified"),
        Map.entry("minecraft:magma_cube", "minecraft:entity.magma_cube.squish_small"),
        Map.entry("minecraft:piglin", "minecraft:entity.piglin.admiring_item"),
        Map.entry("minecraft:piglin_brute", "minecraft:entity.piglin_brute.converted_to_zombified"),
        Map.entry("minecraft:strider", "minecraft:entity.strider.step_lava"),
        Map.entry("minecraft:wither", "minecraft:entity.wither.spawn"),
        Map.entry("minecraft:zoglin", "minecraft:entity.zoglin.angry"),
        Map.entry("minecraft:zombified_piglin", "minecraft:entity.zombified_piglin.angry"),
        Map.entry("minecraft:enderman", "minecraft:entity.enderman.teleport"),
        Map.entry("minecraft:endermite", "minecraft:entity.endermite.step"),
        Map.entry("minecraft:shulker", "minecraft:entity.shulker.teleport"),
        Map.entry("minecraft:ender_dragon", "minecraft:entity.ender_dragon.shoot")
    ).forEach((mob, event) -> assertEquals(mob, MobSoundCatalog.mobFor(event, mob)));
    assertEquals("minecraft:spider", MobSoundCatalog.mobFor("minecraft:entity.spider.ambient", "minecraft:cave_spider"));
  }

  @Test void mapsMooshroomSoundsToTheCowControl() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.cow.ambient", "minecraft:mooshroom"));
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.mooshroom.shear", "minecraft:mooshroom"));
    assertEquals("minecraft:cow", MobSoundCatalog.mobForSource("minecraft:mooshroom"));
  }

  @Test void sharedEventsRequireTheirSourceMob() {
    assertEquals("minecraft:cow", MobSoundCatalog.mobFor("minecraft:entity.generic.swim", "minecraft:cow"));
    assertNull(MobSoundCatalog.mobFor("minecraft:entity.generic.swim", null));
  }

  @Test void exposesEveryMobCoveredByASharedControl() {
    assertEquals(java.util.List.of("minecraft:cow", "minecraft:mooshroom"), MobSoundCatalog.variantsFor("minecraft:cow"));
    assertEquals(java.util.List.of("minecraft:spider", "minecraft:cave_spider"), MobSoundCatalog.variantsFor("minecraft:spider"));
    assertEquals("cow/mooshroom", MobSoundCatalog.displayName("minecraft:cow"));
  }

  @Test void mapsEntitySpecificSoundsForAddedCreatures() {
    Map.ofEntries(
        Map.entry("minecraft:camel", "minecraft:entity.camel.dash_ready"),
        Map.entry("minecraft:cod", "minecraft:entity.cod.flop"),
        Map.entry("minecraft:dolphin", "minecraft:entity.dolphin.play"),
        Map.entry("minecraft:frog", "minecraft:entity.frog.tongue"),
        Map.entry("minecraft:glow_squid", "minecraft:entity.glow_squid.squirt"),
        Map.entry("minecraft:nautilus", "minecraft:entity.nautilus.riding"),
        Map.entry("minecraft:polar_bear", "minecraft:entity.polar_bear.warning"),
        Map.entry("minecraft:pufferfish", "minecraft:entity.puffer_fish.sting"),
        Map.entry("minecraft:salmon", "minecraft:entity.salmon.flop"),
        Map.entry("minecraft:squid", "minecraft:entity.squid.squirt"),
        Map.entry("minecraft:tadpole", "minecraft:entity.tadpole.grow_up"),
        Map.entry("minecraft:tropical_fish", "minecraft:entity.tropical_fish.flop"),
        Map.entry("minecraft:wandering_trader", "minecraft:entity.wandering_trader.reappeared"),
        Map.entry("minecraft:wither_skeleton", "minecraft:entity.wither_skeleton.ambient"),
        Map.entry("minecraft:illusioner", "minecraft:entity.illusioner.prepare_mirror")
    ).forEach((mob, event) -> assertEquals(mob, MobSoundCatalog.mobFor(event, mob)));
  }
}
