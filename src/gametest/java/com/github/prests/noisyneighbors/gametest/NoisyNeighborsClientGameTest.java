package com.github.prests.noisyneighbors.gametest;

import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Verifies the real client, mod entrypoint, and mixins load in a disposable world. */
@SuppressWarnings("UnstableApiUsage")
public final class NoisyNeighborsClientGameTest implements FabricClientGameTest {
  @Override public void runTest(ClientGameTestContext context) {
    try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
      singleplayer.getConnection().waitForChunksRender();
      if (!MobSoundCatalog.isKnown("minecraft:entity.cow.ambient")) {
        throw new AssertionError("client initializer did not load the sound catalog");
      }
    }
  }
}
