package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.sound.VolumePolicy;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
abstract class SoundEngineMixin {
  private final Map<SoundInstance, Float> noisyNeighbors$multipliers = Collections.synchronizedMap(new WeakHashMap<>());
  private final ThreadLocal<SoundInstance> noisyNeighbors$startingSound = new ThreadLocal<>();

  @Shadow protected abstract float calculateVolume(float volume, SoundSource source);

  @Inject(method = "play", at = @At("HEAD"))
  private void noisyNeighbors$captureStartingSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
    noisyNeighbors$multipliers.computeIfAbsent(sound, this::noisyNeighbors$multiplierAtStart);
    noisyNeighbors$startingSound.set(sound);
  }

  @Inject(method = "play", at = @At("RETURN"))
  private void noisyNeighbors$clearStartingSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
    noisyNeighbors$startingSound.remove();
  }

  @Redirect(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
  private float noisyNeighbors$applyInitialVolume(SoundEngine engine, float volume, SoundSource source) {
    SoundInstance sound = noisyNeighbors$startingSound.get();
    return calculateVolume(volume, source) * (sound == null ? 1F : noisyNeighbors$multipliers.getOrDefault(sound, 1F));
  }

  @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("RETURN"), cancellable = true)
  private void noisyNeighbors$applyVolume(SoundInstance sound, CallbackInfoReturnable<Float> callback) {
    float multiplier = noisyNeighbors$multipliers.computeIfAbsent(sound, this::noisyNeighbors$multiplierAtStart);
    callback.setReturnValue(callback.getReturnValueF() * multiplier);
  }

  private float noisyNeighbors$multiplierAtStart(SoundInstance sound) {
    Entity entity = sound instanceof EntityBoundSoundAccessor accessor ? accessor.noisyNeighbors$entity() : null;
    return VolumePolicy.multiplier(sound.getIdentifier().toString(), entity, sound.getX(), sound.getY(), sound.getZ());
  }
}
