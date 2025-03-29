package com.github.prests.noisyneighbors.screen;

import com.github.prests.noisyneighbors.config.GlobalEntityVolumeConfig;
import com.github.prests.noisyneighbors.slider.PigSlider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class MainEntitySoundControlScreen extends GameOptionsScreen {
    private static final GlobalEntityVolumeConfig globalEntityVolumeConfig = GlobalEntityVolumeConfig.getInstance();

    final static Text ENTITY_SOUND_CONTROL_SCREEN_TITLE = Text.of("Entity Sound Control");


    final PigSlider pigSlider;

    public MainEntitySoundControlScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, ENTITY_SOUND_CONTROL_SCREEN_TITLE);

        pigSlider = new PigSlider();
    }

    @Override
    protected void addOptions() {
        if (this.body == null) {
            return;
        }

        this.body.addWidgetEntry(pigSlider.getSlider(), null);
    }

    @Override
    public void close() {
        globalEntityVolumeConfig.save();

        super.close();
    }
}