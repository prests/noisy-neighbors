package com.github.prests.noisyneighbors.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;

@Mixin(EntityBoundSoundInstance.class)
public interface EntityBoundSoundAccessor {
  @Accessor("entity") Entity noisyNeighbors$entity();
}
