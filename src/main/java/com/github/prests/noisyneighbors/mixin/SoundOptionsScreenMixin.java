package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.ui.GlobalSoundScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
abstract class SoundOptionsScreenMixin {
  @Inject(method = "addOptions", at = @At("TAIL"))
  private void noisyNeighbors$addButton(CallbackInfo ci) {
    SoundOptionsScreen screen = (SoundOptionsScreen) (Object) this;
    ((OptionsSubScreenAccessor) screen).noisyNeighbors$list().addBig(Button.builder(Component.translatable("noisy-neighbors.open"), button -> Minecraft.getInstance().gui.setScreen(new GlobalSoundScreen(screen))).width(310).build());
  }
}
