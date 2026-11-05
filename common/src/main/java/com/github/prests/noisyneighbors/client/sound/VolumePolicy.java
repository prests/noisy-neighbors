package com.github.prests.noisyneighbors.client.sound;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class VolumePolicy {
  public record Evaluation(double chattiness, float multiplier) {}

  private VolumePolicy() {}

  public static Evaluation evaluate(String eventId, Entity source, double x, double y, double z) {
    Minecraft client = Minecraft.getInstance();
    if (client.level == null) return new Evaluation(1, 1F);
    String sourceMob = source == null ? null : mobId(source);
    if (source != null && sourceMob == null) return new Evaluation(1, 1F);
    WorldIdentity world = WorldIdentity.current(client);
    if (world == null) return new Evaluation(1, 1F);
    return evaluate(SettingsStore.data(), eventId, sourceMob, world.key(), client.level.dimension().identifier().toString(), x, y, z);
  }

  public static Evaluation evaluate(SettingsStore.Data data, String eventId, String sourceMobId, String world, String dimension,
                                    double x, double y, double z) {
    String mob = MobSoundCatalog.mobFor(eventId, sourceMobId);
    if (mob == null) return new Evaluation(1, 1F);

    double chattiness = data.chattiness.getOrDefault(mob, 100) / 100.0;
    double volume = data.global.getOrDefault(mob, 100) / 100.0;
    volume *= data.events.getOrDefault(mob, Map.of()).getOrDefault(eventId, 100) / 100.0;
    SettingsStore.World settings = data.worlds.get(world);
    if (settings != null) for (Zone zone : settings.zones) {
      if (zone.enabled() && zone.dimension().equals(dimension) && zone.contains(x, y, z)) {
        chattiness *= zone.chattinessMultiplier(mob);
        volume *= zone.multiplier(mob) * zone.eventMultiplier(mob, eventId);
      }
    }
    return new Evaluation(chattiness, (float) volume);
  }

  public static float multiplier(String eventId, Entity source, double x, double y, double z) {
    return evaluate(eventId, source, x, y, z).multiplier();
  }

  public static float multiplier(String eventId, String sourceMobId, String world, String dimension, double x, double y, double z) {
    return evaluate(SettingsStore.data(), eventId, sourceMobId, world, dimension, x, y, z).multiplier();
  }

  public static double chattiness(String eventId, Entity source, double x, double y, double z) {
    return evaluate(eventId, source, x, y, z).chattiness();
  }

  public static double chattiness(SettingsStore.Data data, String eventId, String sourceMobId, String world, String dimension,
                                  double x, double y, double z) {
    return evaluate(data, eventId, sourceMobId, world, dimension, x, y, z).chattiness();
  }

  public static boolean passesChattiness(double chattiness, double roll) {
    return roll < chattiness;
  }

  public static float multiplier(SettingsStore.Data data, String eventId, String sourceMobId, String world, String dimension,
                                 double x, double y, double z) {
    return evaluate(data, eventId, sourceMobId, world, dimension, x, y, z).multiplier();
  }

  private static String mobId(Entity entity) {
    if (!(entity instanceof LivingEntity) || entity instanceof Player) return null;
    return MobSoundCatalog.mobForSource(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
  }

  public static float apply(float originalVolume, float multiplier) {
    return originalVolume * multiplier;
  }

  static float multiplier(Map<String, Integer> global, Zone... zones) {
    double result = global.getOrDefault("minecraft:cow", 100) / 100.0;
    for (Zone zone : zones) result *= zone.multiplier("minecraft:cow");
    return (float) result;
  }
}
