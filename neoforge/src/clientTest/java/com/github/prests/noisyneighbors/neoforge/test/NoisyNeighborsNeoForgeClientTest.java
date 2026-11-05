package com.github.prests.noisyneighbors.neoforge.test;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.ZoneSelection;
import com.github.prests.noisyneighbors.clienttest.ClientTestAssertions;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Test-only installed mod that validates the production NeoForge JAR in a real client. */
@Mod(value = "noisy_neighbors_test", dist = Dist.CLIENT)
public final class NoisyNeighborsNeoForgeClientTest {
  private int phase;
  private int renderedTicks;
  private ClientTestAssertions.Outline outline;
  private boolean replaceNextSound;
  private boolean replacementObserved;

  public NoisyNeighborsNeoForgeClientTest(IEventBus modBus) {
    NeoForge.EVENT_BUS.addListener(this::clientTick);
    NeoForge.EVENT_BUS.addListener(this::replaceSound);
  }

  private void clientTick(ClientTickEvent.Post event) {
    Minecraft client = Minecraft.getInstance();
    if (client.level == null || client.player == null) return;
    try {
      if (phase == 0) {
        ClientTestAssertions.verifyLoaded();
        ClientTestAssertions.verifySoundMixinRejectsMutedSounds(client);
        ClientTestAssertions.verifyEntitySearchFiltersBothMenus(client);
        ClientTestAssertions.verifyFineGrainedScreens(client);
        ClientTestAssertions.clickWorldZonesHotkey(client);
        phase = 1;
      } else if (phase == 1) {
        ClientTestAssertions.assertWorldZonesOpened(client);
        verifyLoaderCallbacks(client);
        verifyReplacementSound(client);
        outline = ClientTestAssertions.enableDisposableOutline(client);
        phase = 2;
      } else if (phase == 2 && ++renderedTicks >= 5) {
        ClientTestAssertions.removeDisposableOutline(client, outline);
        Files.writeString(Path.of("noisy-neighbors-neoforge-success"), "ok\n");
        phase = 3;
      }
    } catch (Throwable throwable) {
      NoisyNeighbors.LOGGER.error("NeoForge production client test failed", throwable);
      throw new AssertionError("NeoForge production client test failed", throwable);
    }
  }

  private void verifyLoaderCallbacks(Minecraft client) {
    ZoneSelection.begin();
    var pos = client.player.blockPosition();
    var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    var event = new PlayerInteractEvent.RightClickBlock(client.player, InteractionHand.MAIN_HAND, pos, hit);
    NeoForge.EVENT_BUS.post(event);
    if (!event.isCanceled() || !ZoneSelection.isActive()) {
      throw new AssertionError("NeoForge right-click callback did not consume the active selection");
    }
    NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut(null, null, null));
    if (ZoneSelection.isActive()) {
      throw new AssertionError("NeoForge logout callback did not clear the active selection");
    }
  }

  private void verifyReplacementSound(Minecraft client) {
    Integer previous = SettingsStore.data().global.put("minecraft:cow", 0);
    try {
      replaceNextSound = true;
      replacementObserved = false;
      ClientTestAssertions.assertRejected(client,
          ClientTestAssertions.sound(client, "minecraft:block.note_block.harp"), "NeoForge replacement");
      if (!replacementObserved) throw new AssertionError("NeoForge PlaySoundEvent did not replace the test sound");
    } finally {
      replaceNextSound = false;
      if (previous == null) SettingsStore.data().global.remove("minecraft:cow");
      else SettingsStore.data().global.put("minecraft:cow", previous);
    }
  }

  private void replaceSound(PlaySoundEvent event) {
    if (!replaceNextSound) return;
    replaceNextSound = false;
    replacementObserved = true;
    event.setSound(ClientTestAssertions.sound(Minecraft.getInstance(), "minecraft:entity.cow.ambient"));
  }
}
