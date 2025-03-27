package com.github.prests.noisyneighbors.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class MainEntitySoundControlScreen extends GameOptionsScreen {
    final static Text ENTITY_SOUND_CONTROL_SCREEN_TITLE = Text.of("Entity Sound Control");

    public MainEntitySoundControlScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, ENTITY_SOUND_CONTROL_SCREEN_TITLE);
    }

    @Override
    protected void addOptions() {

    }
}