package com.github.prests.noisyneighbors.client.config;

import com.github.prests.noisyneighbors.mixin.MinecraftServerAccessor;
import java.util.Locale;
import net.minecraft.client.Minecraft;

/** Local saves use their folder; servers intentionally use endpoint only. */
public record WorldIdentity(String key, String displayName) {
  public static WorldIdentity current(Minecraft client) {
    if (client.getCurrentServer() != null) return server(client.getCurrentServer().ip, client.getCurrentServer().name);
    if (client.getSingleplayerServer() == null) return null;
    String directory = ((MinecraftServerAccessor) client.getSingleplayerServer()).noisyNeighbors$storageSource().getLevelId();
    return local(directory, client.getSingleplayerServer().getWorldData().getLevelName());
  }

  public static WorldIdentity local(String directoryId, String displayName) {
    return new WorldIdentity("local:" + directoryId, displayName);
  }

  public static WorldIdentity server(String endpoint, String displayName) {
    return new WorldIdentity("server:" + endpoint.trim().toLowerCase(Locale.ROOT), displayName);
  }
}
