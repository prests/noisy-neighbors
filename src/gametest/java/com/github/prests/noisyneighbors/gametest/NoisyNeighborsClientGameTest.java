package com.github.prests.noisyneighbors.gametest;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.lang.reflect.Field;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Verifies the real client, mod entrypoint, and sound mixin load in a disposable world. */
@SuppressWarnings("UnstableApiUsage")
public final class NoisyNeighborsClientGameTest implements FabricClientGameTest {
  @Override public void runTest(ClientGameTestContext context) {
    try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
      singleplayer.getConnection().waitForChunksRender();
      if (!MobSoundCatalog.isKnown("minecraft:entity.cow.ambient")) {
        throw new AssertionError("client initializer did not load the sound catalog");
      }
      verifySoundMixinCapturesConfiguredVolume();
    }
  }

  private static void verifySoundMixinCapturesConfiguredVolume() {
    Integer previous = SettingsStore.data().global.put("minecraft:cow", 0);
    try {
      Minecraft client = Minecraft.getInstance();
      SimpleSoundInstance sound = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(Identifier.parse("minecraft:entity.cow.ambient")),
          SoundSource.NEUTRAL, 1F, 1F,
          SoundInstance.createUnseededRandom(), client.player.getX(), client.player.getY(), client.player.getZ());
      SoundManager manager = client.getSoundManager();
      manager.play(sound);
      Float multiplier = multipliers(manager).get(sound);
      if (multiplier == null || multiplier != 0F) {
        throw new AssertionError("sound mixin did not capture the configured cow volume: " + multiplier);
      }
    } finally {
      if (previous == null) SettingsStore.data().global.remove("minecraft:cow");
      else SettingsStore.data().global.put("minecraft:cow", previous);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<SoundInstance, Float> multipliers(SoundManager manager) {
    try {
      Field engine = SoundManager.class.getDeclaredField("soundEngine");
      engine.setAccessible(true);
      Field multipliers = SoundEngine.class.getDeclaredField("noisyNeighbors$multipliers");
      multipliers.setAccessible(true);
      return (Map<SoundInstance, Float>) multipliers.get(engine.get(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound mixin test hook is unavailable", exception);
    }
  }
}
