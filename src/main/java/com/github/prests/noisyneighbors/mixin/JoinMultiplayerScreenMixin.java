package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
abstract class JoinMultiplayerScreenMixin {
  @Shadow private ServerSelectionList serverSelectionList;

  @Inject(method = "deleteCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerList;remove(Lnet/minecraft/client/multiplayer/ServerData;)V", shift = At.Shift.AFTER))
  private void noisyNeighbors$deleteServerZones(boolean confirmed, CallbackInfo callback) {
    ServerSelectionList.Entry entry = serverSelectionList.getSelected();
    if (entry instanceof ServerSelectionList.OnlineServerEntry server) {
      SettingsStore.removeWorld(WorldIdentity.server(server.getServerData().ip, server.getServerData().name).key());
    }
  }
}
