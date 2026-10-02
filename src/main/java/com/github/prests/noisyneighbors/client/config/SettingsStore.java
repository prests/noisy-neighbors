package com.github.prests.noisyneighbors.client.config;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.fabricmc.loader.api.FabricLoader;

/** Client-only, sparse settings. Invalid settings are never used. */
public final class SettingsStore {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("noisy-neighbors.json");
  private static Data data = new Data();

  private SettingsStore() {}
  public static Data data() { return data; }

  public static void removeWorld(String worldKey) {
    if (data.worlds.remove(worldKey) != null) save();
  }

  public static void removeZone(String worldKey, UUID zoneId) {
    World world = data.worlds.get(worldKey);
    if (world != null) world.zones.removeIf(zone -> zone.id().equals(zoneId));
  }

  public static void updateZone(String worldKey, UUID zoneId, UnaryOperator<Zone> update) {
    World world = data.worlds.get(worldKey);
    if (world == null) return;
    for (int index = 0; index < world.zones.size(); index++) {
      Zone zone = world.zones.get(index);
      if (zone.id().equals(zoneId)) {
        world.zones.set(index, update.apply(zone));
        return;
      }
    }
  }

  public static void load() {
    if (!Files.exists(FILE)) return;
    try {
      Data loaded = GSON.fromJson(Files.readString(FILE), Data.class);
      if (loaded == null || loaded.version != 1) throw new IOException("Unsupported settings version");
      loaded.validate();
      data = loaded;
    } catch (Exception exception) {
      NoisyNeighbors.LOGGER.error("Ignoring corrupt Noisy Neighbor settings at {}", FILE, exception);
    }
  }

  public static void save() {
    data.validate();
    Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
    try {
      Files.createDirectories(FILE.getParent());
      Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
      try {
        Files.move(temporary, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ignored) {
        Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      NoisyNeighbors.LOGGER.error("Could not save Noisy Neighbor settings; keeping existing file", exception);
      try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
    }
  }

  public static final class Data {
    int version = 1;
    public Map<String, Integer> global = new HashMap<>();
    public Map<String, World> worlds = new HashMap<>();
    public boolean showZoneOutlines;
    public void validate() {
      global.entrySet().removeIf(entry -> !valid(entry.getKey(), entry.getValue()));
      worlds.values().forEach(World::validate);
    }
  }

  public static final class World {
    public String displayName = "World";
    public List<Zone> zones = new ArrayList<>();
    void validate() { zones.removeIf(zone -> zone == null || !zone.volumes().entrySet().stream().allMatch(e -> valid(e.getKey(), e.getValue()))); }
  }

  private static boolean valid(String id, Integer value) { return id != null && !id.isBlank() && value != null && value >= 0 && value <= 100; }
}
