package com.github.prests.noisyneighbors.client.config;

import java.util.Locale;
import net.minecraft.client.Minecraft;

/** Local saves use their folder; servers intentionally use endpoint only. */
public record WorldIdentity(String key, String displayName) {
  public static WorldIdentity current(Minecraft client) {
    if (client.getCurrentServer() != null) return server(client.getCurrentServer().ip, client.getCurrentServer().name);
    if (client.getSingleplayerServer() == null) return null;
    String directory = client.getSingleplayerServer().getServerDirectory().getFileName().toString();
    return local(directory, directory);
  }

  public static WorldIdentity local(String directoryId, String displayName) {
    return new WorldIdentity("local:" + directoryId, displayName);
  }

  public static WorldIdentity server(String endpoint, String displayName) {
    return new WorldIdentity("server:" + endpoint.trim().toLowerCase(Locale.ROOT), displayName);
  }
}
