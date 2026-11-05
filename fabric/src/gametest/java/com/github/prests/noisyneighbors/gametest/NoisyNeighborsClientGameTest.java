package com.github.prests.noisyneighbors.gametest;

import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import com.github.prests.noisyneighbors.clienttest.ClientTestAssertions;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Verifies the remapped Fabric artifact and its loader callbacks in a disposable world. */
@SuppressWarnings("UnstableApiUsage")
public final class NoisyNeighborsClientGameTest implements FabricClientGameTest {
  @Override
  public void runTest(ClientGameTestContext context) {
    try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
      singleplayer.getConnection().waitForChunksRender();
      context.runOnClient(client -> {
        ClientTestAssertions.verifyLoaded();
        ClientTestAssertions.verifySoundMixinRejectsMutedSounds(client);
        ClientTestAssertions.verifyEntitySearchFiltersBothMenus(client);
        ClientTestAssertions.verifyFineGrainedScreens(client);
        ClientTestAssertions.clickWorldZonesHotkey(client);
      });
      context.waitTick();
      ClientTestAssertions.Outline outline = context.computeOnClient(client -> {
        ClientTestAssertions.assertWorldZonesOpened(client);
        verifyLoaderCallbacks(client);
        return ClientTestAssertions.enableDisposableOutline(client);
      });
      context.waitTicks(5);
      context.runOnClient(client -> ClientTestAssertions.removeDisposableOutline(client, outline));
    }
  }

  private static void verifyLoaderCallbacks(net.minecraft.client.Minecraft client) {
    ZoneSelection.begin();
    var pos = client.player.blockPosition();
    var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    InteractionResult result = UseBlockCallback.EVENT.invoker().interact(client.player, client.level, InteractionHand.MAIN_HAND, hit);
    if (result != InteractionResult.FAIL || !ZoneSelection.isActive()) {
      throw new AssertionError("Fabric use-block callback did not consume the active selection");
    }
    ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(client.getConnection(), client);
    if (ZoneSelection.isActive()) {
      throw new AssertionError("Fabric disconnect callback did not clear the active selection");
    }
  }
}
