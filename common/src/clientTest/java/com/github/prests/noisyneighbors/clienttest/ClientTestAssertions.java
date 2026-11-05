package com.github.prests.noisyneighbors.clienttest;

import com.github.prests.noisyneighbors.client.config.SettingsStore;
import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import com.github.prests.noisyneighbors.client.ui.FineGrainedSoundScreen;
import com.github.prests.noisyneighbors.client.ui.GlobalSoundScreen;
import com.github.prests.noisyneighbors.client.ui.WorldZonesScreen;
import com.github.prests.noisyneighbors.client.ui.ZoneSoundScreen;
import com.github.prests.noisyneighbors.client.zone.Zone;
import com.github.prests.noisyneighbors.mixin.OptionsSubScreenAccessor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Loader-neutral assertions exercised by each production client artifact. */
public final class ClientTestAssertions {
  private ClientTestAssertions() {}

  public static void verifyLoaded() {
    if (!MobSoundCatalog.isKnown("minecraft:entity.cow.ambient")) {
      throw new AssertionError("client initializer did not load the sound catalog");
    }
  }

  public static void verifySoundMixinRejectsMutedSounds(Minecraft client) {
    Integer previousVolume = SettingsStore.data().global.put("minecraft:cow", 0);
    Integer previousChattiness = SettingsStore.data().chattiness.remove("minecraft:cow");
    try {
      assertRejected(client, cowSound(client), "zero volume");
      SettingsStore.data().global.put("minecraft:cow", 100);
      SettingsStore.data().chattiness.put("minecraft:cow", 0);
      assertRejected(client, cowSound(client), "zero chattiness");
    } finally {
      restore(SettingsStore.data().global, "minecraft:cow", previousVolume);
      restore(SettingsStore.data().chattiness, "minecraft:cow", previousChattiness);
    }
  }

  public static void assertRejected(Minecraft client, SoundInstance sound, String setting) {
    SoundManager manager = client.getSoundManager();
    int[] notifications = {0};
    SoundEventListener listener = (played, events, range) -> notifications[0]++;
    manager.addListener(listener);
    try {
      SoundEngine.PlayResult result = manager.play(sound);
      if (result != SoundEngine.PlayResult.NOT_STARTED || multipliers(manager).containsKey(sound)
          || channels(manager).containsKey(sound) || manager.isActive(sound) || notifications[0] != 0) {
        throw new AssertionError(setting + " sound was not rejected before listeners or channel allocation: " + result);
      }
    } finally {
      manager.removeListener(listener);
    }
  }

  public static SoundInstance sound(Minecraft client, String id) {
    return new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(Identifier.parse(id)), SoundSource.NEUTRAL, 1F, 1F,
        SoundInstance.createUnseededRandom(), client.player.getX(), client.player.getY(), client.player.getZ());
  }

  public static void verifyEntitySearchFiltersBothMenus(Minecraft client) {
    WorldIdentity world = requireWorld(client);
    UUID zoneId = UUID.randomUUID();
    SettingsStore.addZone(world, new Zone(zoneId, "Search test", true, 0xFFFFFF,
        client.level.dimension().identifier().toString(), 0, 0, 0, 1, 1, 1, Map.of()));
    try {
      GlobalSoundScreen global = new GlobalSoundScreen(null);
      client.gui.setScreen(global);
      assertSearchResults(global, "moo", "cow/mooshroom");

      ZoneSoundScreen zone = new ZoneSoundScreen(null, world.key(), zoneId);
      client.gui.setScreen(zone);
      assertSearchResults(zone, "cave", "spider/cave_spider");
    } finally {
      SettingsStore.removeZone(world.key(), zoneId);
      client.gui.setScreen(null);
    }
  }

  public static void verifyFineGrainedScreens(Minecraft client) {
    FineGrainedSoundScreen global = new FineGrainedSoundScreen(null, "minecraft:cow");
    client.gui.setScreen(global);
    if (!sliderLabels(((OptionsSubScreenAccessor) (Object) global).noisyNeighbors$list()).contains("minecraft:entity.cow.hurt: 100%")) {
      throw new AssertionError("global advanced controls did not render exact event IDs");
    }
    client.gui.setScreen(null);
  }

  public static void clickWorldZonesHotkey(Minecraft client) {
    client.gui.setScreen(null);
    assertDefault("key.noisy-neighbors.toggle_outlines", "key.keyboard.k");
    assertDefault("key.noisy-neighbors.create_zone", "key.keyboard.z");
    KeyMapping hotkey = assertDefault("key.noisy-neighbors.open_world_zones", "key.keyboard.u");
    hotkey.setKey(hotkey.getDefaultKey());
    KeyMapping.resetMapping();
    KeyMapping.click(hotkey.getDefaultKey());
  }

  public static void assertWorldZonesOpened(Minecraft client) {
    if (!(client.gui.screen() instanceof WorldZonesScreen)) {
      throw new AssertionError("world zones hotkey did not open through the loader tick callback");
    }
    client.gui.setScreen(null);
  }

  public static Outline enableDisposableOutline(Minecraft client) {
    WorldIdentity world = requireWorld(client);
    UUID zoneId = UUID.randomUUID();
    int x = client.player.getBlockX();
    int y = client.player.getBlockY();
    int z = client.player.getBlockZ();
    SettingsStore.addZone(world, new Zone(zoneId, "Render test", true, 0x33E5FF,
        client.level.dimension().identifier().toString(), x, y, z, x + 1, y + 1, z + 1, Map.of()));
    boolean previousVisible = SettingsStore.data().showZoneOutlines;
    SettingsStore.data().showZoneOutlines = true;
    return new Outline(zoneId, previousVisible);
  }

  public static void removeDisposableOutline(Minecraft client, Outline outline) {
    WorldIdentity world = requireWorld(client);
    SettingsStore.removeZone(world.key(), outline.zoneId());
    SettingsStore.data().showZoneOutlines = outline.previousVisible();
  }

  public record Outline(UUID zoneId, boolean previousVisible) {}

  private static SoundInstance cowSound(Minecraft client) {
    return sound(client, "minecraft:entity.cow.ambient");
  }

  private static KeyMapping assertDefault(String id, String expected) {
    KeyMapping mapping = KeyMapping.get(id);
    if (mapping == null || !mapping.getDefaultKey().getName().equals(expected)) {
      throw new AssertionError(id + " default is " + (mapping == null ? "missing" : mapping.getDefaultKey().getName()) + " instead of " + expected);
    }
    return mapping;
  }

  private static WorldIdentity requireWorld(Minecraft client) {
    WorldIdentity world = WorldIdentity.current(client);
    if (world == null) throw new AssertionError("singleplayer world identity is unavailable");
    return world;
  }

  private static void restore(Map<String, Integer> values, String key, Integer previous) {
    if (previous == null) values.remove(key);
    else values.put(key, previous);
  }

  private static void assertSearchResults(Screen screen, String query, String expectedTitle) {
    search(screen).setValue(query);
    OptionsList list = ((OptionsSubScreenAccessor) screen).noisyNeighbors$list();
    List<String> titles = entityTitles(list);
    List<String> labels = sliderLabels(list);
    if (!titles.equals(List.of(expectedTitle)) || !labels.equals(List.of("Volume: 100%", "Chattiness: 100%"))) {
      throw new AssertionError("search '" + query + "' showed titles " + titles + " and controls " + labels);
    }
  }

  private static List<String> entityTitles(OptionsList list) {
    List<String> titles = new ArrayList<>();
    for (Object entry : (List<?>) list.children()) {
      try {
        Field title = entry.getClass().getDeclaredField("title");
        title.setAccessible(true);
        titles.add(((net.minecraft.network.chat.Component) title.get(entry)).getString());
      } catch (NoSuchFieldException ignored) {
      } catch (ReflectiveOperationException exception) {
        throw new AssertionError("entity row titles are unavailable", exception);
      }
    }
    return titles;
  }

  private static List<String> sliderLabels(OptionsList list) {
    try {
      List<String> labels = new ArrayList<>();
      for (Object entry : (List<?>) list.children()) {
        Field children = entry.getClass().getDeclaredField("children");
        children.setAccessible(true);
        for (Object child : (List<?>) children.get(entry)) {
          AbstractWidget widget = (AbstractWidget) child.getClass().getMethod("widget").invoke(child);
          if (widget instanceof AbstractSliderButton slider) labels.add(slider.getMessage().getString());
        }
      }
      return labels;
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("options list widgets are unavailable", exception);
    }
  }

  private static EditBox search(Screen screen) {
    try {
      Field search = screen.getClass().getDeclaredField("search");
      search.setAccessible(true);
      return (EditBox) search.get(screen);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("search field is unavailable", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<SoundInstance, Float> multipliers(SoundManager manager) {
    try {
      Field multipliers = SoundEngine.class.getDeclaredField("noisyNeighbors$multipliers");
      multipliers.setAccessible(true);
      return (Map<SoundInstance, Float>) multipliers.get(engine(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound mixin test hook is unavailable", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<SoundInstance, ?> channels(SoundManager manager) {
    try {
      Field channels = SoundEngine.class.getDeclaredField("instanceToChannel");
      channels.setAccessible(true);
      return (Map<SoundInstance, ?>) channels.get(engine(manager));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("sound engine channels are unavailable", exception);
    }
  }

  private static SoundEngine engine(SoundManager manager) throws ReflectiveOperationException {
    Field engine = SoundManager.class.getDeclaredField("soundEngine");
    engine.setAccessible(true);
    return (SoundEngine) engine.get(manager);
  }
}
