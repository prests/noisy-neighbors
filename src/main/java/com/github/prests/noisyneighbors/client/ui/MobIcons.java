package com.github.prests.noisyneighbors.client.ui;

import com.github.prests.noisyneighbors.client.sound.MobSoundCatalog;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Pixel-art mob portraits, including both mobs when a volume control is shared. */
final class MobIcons {
  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("noisy-neighbors", "textures/gui/mob_icons.png");
  private static final int SPRITE_SIZE = 8;
  private static final int ICON_SIZE = 20;
  static final int WIDTH = ICON_SIZE + 2;
  private static final int SHEET_WIDTH = 104;
  private static final int SHEET_HEIGHT = 120;
  private static final Map<String, Sprite> ICONS = Map.ofEntries(
      Map.entry("allay", sprite(12, 6)),
      Map.entry("armadillo", sprite(12, 10)),
      Map.entry("axolotl", sprite(10, 1)),
      Map.entry("bat", sprite(12, 2)),
      Map.entry("bee", sprite(12, 1)),
      Map.entry("blaze", sprite(8, 11)),
      Map.entry("bogged", sprite(12, 11)),
      Map.entry("breeze", sprite(12, 9)),
      Map.entry("camel", sprite(12, 7)),
      Map.entry("camel_husk", sprite(0, 14)),
      Map.entry("cave_spider", sprite(11, 4)),
      Map.entry("cat", sprite(12, 0)),
      Map.entry("chicken", sprite(11, 2)),
      Map.entry("copper_golem", sprite(1, 14)),
      Map.entry("cod", sprite(11, 1)),
      Map.entry("cow", sprite(10, 10)),
      Map.entry("creaking", sprite(11, 13)),
      Map.entry("creeper", sprite(7, 10)),
      Map.entry("dolphin", sprite(3, 10)),
      Map.entry("donkey", sprite(2, 10)),
      Map.entry("drowned", sprite(1, 10)),
      Map.entry("elder_guardian", sprite(0, 10)),
      Map.entry("ender_dragon", sprite(10, 9)),
      Map.entry("enderman", sprite(10, 8)),
      Map.entry("endermite", sprite(10, 7)),
      Map.entry("evoker", sprite(10, 6)),
      Map.entry("illusioner", sprite(9, 6)),
      Map.entry("fox", sprite(6, 2)),
      Map.entry("frog", sprite(0, 5)),
      Map.entry("ghast", sprite(10, 4)),
      Map.entry("glow_squid", sprite(10, 3)),
      Map.entry("goat", sprite(10, 2)),
      Map.entry("guardian", sprite(1, 9)),
      Map.entry("happy_ghast", sprite(12, 13)),
      Map.entry("hoglin", sprite(0, 9)),
      Map.entry("horse", sprite(9, 8)),
      Map.entry("husk", sprite(9, 7)),
      Map.entry("iron_golem", sprite(9, 5)),
      Map.entry("llama", sprite(4, 3)),
      Map.entry("magma_cube", sprite(4, 8)),
      Map.entry("mooshroom", sprite(3, 8)),
      Map.entry("mule", sprite(2, 8)),
      Map.entry("nautilus", sprite(2, 14)),
      Map.entry("ocelot", sprite(1, 8)),
      Map.entry("panda", sprite(8, 6)),
      Map.entry("polar_bear", sprite(3, 7)),
      Map.entry("pufferfish", sprite(2, 7)),
      Map.entry("parched", sprite(3, 14)),
      Map.entry("parrot", sprite(1, 2)),
      Map.entry("phantom", sprite(8, 4)),
      Map.entry("pig", sprite(8, 3)),
      Map.entry("piglin", sprite(8, 2)),
      Map.entry("piglin_brute", sprite(8, 1)),
      Map.entry("pillager", sprite(8, 0)),
      Map.entry("rabbit", sprite(10, 0)),
      Map.entry("ravager", sprite(7, 4)),
      Map.entry("salmon", sprite(6, 6)),
      Map.entry("sheep", sprite(4, 1)),
      Map.entry("shulker", sprite(3, 6)),
      Map.entry("silverfish", sprite(1, 6)),
      Map.entry("skeleton", sprite(0, 6)),
      Map.entry("skeleton_horse", sprite(6, 5)),
      Map.entry("slime", sprite(6, 3)),
      Map.entry("sniffer", sprite(12, 8)),
      Map.entry("snow_golem", sprite(4, 6)),
      Map.entry("spider", sprite(6, 1)),
      Map.entry("stray", sprite(5, 5)),
      Map.entry("squid", sprite(6, 0)),
      Map.entry("strider", sprite(1, 4)),
      Map.entry("sulfur_cube", sprite(5, 14)),
      Map.entry("tadpole", sprite(2, 5)),
      Map.entry("trader_llama", sprite(2, 3)),
      Map.entry("tropical_fish", sprite(5, 3)),
      Map.entry("turtle", sprite(5, 2)),
      Map.entry("vex", sprite(5, 1)),
      Map.entry("villager", sprite(4, 7)),
      Map.entry("vindicator", sprite(5, 0)),
      Map.entry("wandering_trader", sprite(4, 4)),
      Map.entry("warden", sprite(3, 4)),
      Map.entry("wither_skeleton", sprite(3, 1)),
      Map.entry("witch", sprite(0, 3)),
      Map.entry("wither", sprite(3, 2)),
      Map.entry("wolf", sprite(2, 2)),
      Map.entry("zoglin", sprite(1, 0)),
      Map.entry("zombie", sprite(2, 0)),
      Map.entry("zombie_horse", sprite(1, 1)),
      Map.entry("zombie_nautilus", sprite(4, 14)),
      Map.entry("zombie_villager", sprite(0, 1)),
      Map.entry("zombified_piglin", sprite(0, 0)));

  private MobIcons() {}

  static void draw(GuiGraphicsExtractor graphics, String mob, int x, int y) {
    List<String> variants = MobSoundCatalog.variantsFor(mob);
    if (variants.size() == 1) {
      draw(graphics, variants.getFirst(), x, y, ICON_SIZE);
      return;
    }
    drawHalf(graphics, variants.getFirst(), x, y, false);
    drawHalf(graphics, variants.get(1), x + ICON_SIZE / 2, y, true);
  }

  private static void draw(GuiGraphicsExtractor graphics, String mob, int x, int y, int height) {
    Sprite sprite = icon(mob);
    if (sprite == null) return;
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, sprite.x * SPRITE_SIZE, sprite.y * SPRITE_SIZE,
        ICON_SIZE, height, SPRITE_SIZE, SPRITE_SIZE, SHEET_WIDTH, SHEET_HEIGHT);
  }

  private static void drawHalf(GuiGraphicsExtractor graphics, String mob, int x, int y, boolean right) {
    Sprite sprite = icon(mob);
    if (sprite == null) return;
    int half = SPRITE_SIZE / 2;
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, sprite.x * SPRITE_SIZE + (right ? half : 0), sprite.y * SPRITE_SIZE,
        ICON_SIZE / 2, ICON_SIZE, half, SPRITE_SIZE, SHEET_WIDTH, SHEET_HEIGHT);
  }

  private static Sprite icon(String mob) { return ICONS.get(mob.replace("minecraft:", "")); }

  private static Sprite sprite(int x, int y) { return new Sprite(x, y); }
  private record Sprite(int x, int y) {}
}
