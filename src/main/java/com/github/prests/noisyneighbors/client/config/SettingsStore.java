package com.github.prests.noisyneighbors.client.config;

import com.github.prests.noisyneighbors.NoisyNeighbors;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
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
  private static final Store CLIENT = new Store(FabricLoader.getInstance().getConfigDir().resolve("noisy-neighbors.json"));

  private SettingsStore() {}

  /** Creates an isolated store for tests or tools; the client uses the static facade below. */
  public static Store at(Path file) {
    return new Store(file);
  }

  public static Data data() { return CLIENT.data(); }
  public static void removeWorld(String worldKey) { CLIENT.removeWorld(worldKey); }
  public static void removeZone(String worldKey, UUID zoneId) { CLIENT.removeZone(worldKey, zoneId); }
  public static void updateZone(String worldKey, UUID zoneId, UnaryOperator<Zone> update) { CLIENT.updateZone(worldKey, zoneId, update); }
  public static Zone addZone(WorldIdentity identity, Zone zone) { return CLIENT.addZone(identity, zone); }
  public static void save() { CLIENT.save(); }
  public static void load() { CLIENT.load(); }

  public static final class Store {
    private final Path file;
    private Data data = new Data();

    private Store(Path file) {
      this.file = file;
    }

    public Data data() { return data; }

    public void removeWorld(String worldKey) {
      if (data.worlds.remove(worldKey) != null) save();
    }

    public void removeZone(String worldKey, UUID zoneId) {
      World world = data.worlds.get(worldKey);
      if (world != null) world.zones.removeIf(zone -> zone.id().equals(zoneId));
    }

    public void updateZone(String worldKey, UUID zoneId, UnaryOperator<Zone> update) {
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

    public World world(WorldIdentity identity) {
      return data.worlds.computeIfAbsent(identity.key(), ignored -> {
        World created = new World();
        created.displayName = identity.displayName();
        return created;
      });
    }

    public Zone addZone(WorldIdentity identity, Zone zone) {
      world(identity).zones.add(zone);
      return zone;
    }

    public void load() {
      if (!Files.exists(file)) return;
      try {
        Data loaded = GSON.fromJson(Files.readString(file), Data.class);
        if (loaded == null || loaded.version != 1) throw new IOException("Unsupported settings version");
        loaded.validate();
        data = loaded;
      } catch (Exception exception) {
        NoisyNeighbors.LOGGER.error("Ignoring corrupt Noisy Neighbor settings at {}", file, exception);
      }
    }

    public void save() {
      data.validate();
      Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
      try {
        Files.createDirectories(file.getParent());
        Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
        try {
          Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
          Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException exception) {
        NoisyNeighbors.LOGGER.error("Could not save Noisy Neighbor settings; keeping existing file", exception);
        try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
      }
    }
  }

  public static final class Data {
    int version = 1;
    public Map<String, Integer> global = new HashMap<>();
    /** Sparse canonical entity/event percentages. */
    public Map<String, Map<String, Integer>> events = new HashMap<>();
    public Map<String, World> worlds = new HashMap<>();
    public boolean showZoneOutlines;
    public void validate() {
      if (global == null) global = new HashMap<>();
      if (events == null) events = new HashMap<>();
      if (worlds == null) worlds = new HashMap<>();
      global.entrySet().removeIf(entry -> !valid(entry.getKey(), entry.getValue()));
      normalizeEvents(events);
      worlds.values().removeIf(java.util.Objects::isNull);
      worlds.values().forEach(World::validate);
    }
  }

  public static final class World {
    public String displayName = "World";
    public List<Zone> zones = new ArrayList<>();
    void validate() {
      if (zones == null) zones = new ArrayList<>();
      zones.removeIf(zone -> zone == null || !zone.volumes().entrySet().stream().allMatch(e -> valid(e.getKey(), e.getValue())));
      for (int index = 0; index < zones.size(); index++) {
        Zone zone = zones.get(index);
        Map<String, Map<String, Integer>> events = new HashMap<>(zone.events());
        normalizeEvents(events);
        if (!events.equals(zone.events())) zones.set(index, new Zone(zone.id(), zone.name(), zone.enabled(), zone.color(),
            zone.dimension(), zone.minX(), zone.minY(), zone.minZ(), zone.maxX(), zone.maxY(), zone.maxZ(), zone.volumes(), events));
      }
    }
  }

  private static void normalizeEvents(Map<String, Map<String, Integer>> events) {
    events.entrySet().removeIf(entry -> entry.getKey() == null || !MobSoundCatalog.mobs().contains(entry.getKey())
        || entry.getValue() == null);
    events.replaceAll((mob, values) -> new HashMap<>(values));
    events.forEach((mob, values) -> values.entrySet().removeIf(entry -> !valid(entry.getKey(), entry.getValue())
        || !MobSoundCatalog.controllableEventsFor(mob).contains(entry.getKey()) || entry.getValue() == 100));
    events.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  private static boolean valid(String id, Integer value) {
    return id != null && !id.isBlank() && value != null && value >= 0 && value <= 100;
  }
}
