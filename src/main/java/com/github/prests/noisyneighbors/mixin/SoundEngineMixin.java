package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.client.sound.VolumePolicy;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
abstract class SoundEngineMixin {
  private final Map<SoundInstance, Float> noisyNeighbors$multipliers = Collections.synchronizedMap(new WeakHashMap<>());

  @Shadow protected abstract float calculateVolume(float volume, SoundSource source);

  @Inject(method = "play", at = @At("HEAD"), cancellable = true)
  private void noisyNeighbors$captureStartingSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> callback) {
    Entity entity = sound instanceof EntityBoundSoundAccessor accessor ? accessor.noisyNeighbors$entity() : null;
    VolumePolicy.Evaluation policy = VolumePolicy.evaluate(sound.getIdentifier().toString(), entity, sound.getX(), sound.getY(), sound.getZ());
    if (!VolumePolicy.passesChattiness(policy.chattiness(), ThreadLocalRandom.current().nextDouble()) || policy.multiplier() == 0F) {
      noisyNeighbors$multipliers.remove(sound);
      callback.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
      return;
    }
    noisyNeighbors$multipliers.put(sound, policy.multiplier());
  }

  @Redirect(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
  private float noisyNeighbors$applyInitialVolume(SoundEngine engine, float volume, SoundSource source, SoundInstance sound) {
    return VolumePolicy.apply(calculateVolume(volume, source), noisyNeighbors$multipliers.getOrDefault(sound, 1F));
  }

  @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("RETURN"), cancellable = true)
  private void noisyNeighbors$applyVolume(SoundInstance sound, CallbackInfoReturnable<Float> callback) {
    callback.setReturnValue(VolumePolicy.apply(callback.getReturnValueF(), noisyNeighbors$multipliers.getOrDefault(sound, 1F)));
  }
}
