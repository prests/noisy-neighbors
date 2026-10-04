package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.WorldSelectionList$WorldListEntry")
abstract class WorldListEntryMixin {
  @Shadow @Final private LevelSummary summary;

  @Inject(method = "doDeleteWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;deleteLevel()V", shift = At.Shift.AFTER))
  private void noisyNeighbors$deleteWorldZones(CallbackInfo callback) {
    SettingsStore.removeWorld(WorldIdentity.local(summary.getLevelId(), summary.getLevelName()).key());
  }
}
