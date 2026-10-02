package com.github.prests.noisyneighbors.client.config;

import java.util.Locale;

/** Local saves use their folder; servers intentionally use endpoint only. */
public record WorldIdentity(String key, String displayName) {
  public static WorldIdentity local(String directoryId, String displayName) {
    return new WorldIdentity("local:" + directoryId, displayName);
  }

  public static WorldIdentity server(String endpoint, String displayName) {
    return new WorldIdentity("server:" + endpoint.trim().toLowerCase(Locale.ROOT), displayName);
  }
}
