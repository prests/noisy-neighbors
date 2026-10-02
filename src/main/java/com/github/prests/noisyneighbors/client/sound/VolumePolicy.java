package com.github.prests.noisyneighbors.client.sound;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;

public final class VolumePolicy {
  private VolumePolicy() {}
  public static float multiplier(String eventId, Entity source, double x, double y, double z) {
    String sourceMob = null;
    if (source != null) {
      if (!(source instanceof LivingEntity) || source instanceof Player) return 1F;
      sourceMob = BuiltInRegistries.ENTITY_TYPE.getKey(source.getType()).toString();
      if (!MobSoundCatalog.mobs().contains(sourceMob)) return 1F;
    }
    Minecraft client = Minecraft.getInstance();
    if (client.level == null) return 1F;
    String world = client.getCurrentServer() == null
        ? "local:" + client.getSingleplayerServer().getServerDirectory().getFileName()
        : "server:" + client.getCurrentServer().ip.toLowerCase(java.util.Locale.ROOT);
    return multiplier(eventId, sourceMob, world, client.level.dimension().identifier().toString(), x, y, z);
  }

  public static float multiplier(String eventId, String sourceMobId, String world, String dimension, double x, double y, double z) {
    String mob = MobSoundCatalog.mobFor(eventId, sourceMobId);
    if (mob == null) return 1F;
    SettingsStore.Data data = SettingsStore.data();
    double result = data.global.getOrDefault(mob, 100) / 100.0;
    SettingsStore.World settings = data.worlds.get(world);
    if (settings != null) for (Zone zone : settings.zones) {
      if (zone.dimension().equals(dimension) && zone.contains(x, y, z)) result *= zone.multiplier(mob);
    }
    return (float) result;
  }

  static float multiplier(Map<String, Integer> global, Zone... zones) {
    double result = global.getOrDefault("minecraft:cow", 100) / 100.0;
    for (Zone zone : zones) result *= zone.multiplier("minecraft:cow");
    return (float) result;
  }
}
