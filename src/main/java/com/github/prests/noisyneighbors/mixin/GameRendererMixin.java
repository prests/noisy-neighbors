package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.zone.ZoneOutlines;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
  @Inject(method = "close", at = @At("RETURN"))
  private void noisyNeighbors$closeZoneOutlineBuffer(CallbackInfo callback) {
    ZoneOutlines.close();
  }
}
