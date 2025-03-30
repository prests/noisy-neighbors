package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.config.EntityVolumeSchema;
import com.github.prests.noisyneighbors.config.GlobalEntityVolumeConfig;
import com.github.prests.noisyneighbors.constant.EntityConstants;
import com.github.prests.noisyneighbors.constant.VolumeConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private final static GlobalEntityVolumeConfig globalEntityVolumeConfig = GlobalEntityVolumeConfig.getInstance();

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    public void onEntitySound(SoundEvent sound, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        EntityType<?> type = entity.getType();

        String entityName = EntityConstants.ENTITY_FRIENDLY_NAME_MAP.get(type);
        if (entityName == null) {
            return;
        }

        ci.cancel();

        EntityVolumeSchema entityMainVolumes = globalEntityVolumeConfig.getConfigData().getEntityVolumes().get(entityName);
        if (entityMainVolumes == null) {
            return;
        }

        double mainVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
        double friendlyMobVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.NEUTRAL);

        double volume = (mainVolume * friendlyMobVolume * entityMainVolumes.getMainVolume());

        entity.getWorld().playSoundFromEntity(null, entity, sound, entity.getSoundCategory(), (float) volume, 1.0f);
    }
}
