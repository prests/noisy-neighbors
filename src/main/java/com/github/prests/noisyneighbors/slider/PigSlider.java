package com.github.prests.noisyneighbors.slider;

import com.github.prests.noisyneighbors.config.GlobalEntityVolumeConfig;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class PigSlider {
    private static final GlobalEntityVolumeConfig globalEntityVolumeConfig = GlobalEntityVolumeConfig.getInstance();

    private static final Text title = Text.of("Pig");
    private final SliderWidget slider;

    public PigSlider() {
        slider = SoundSliderFactory.create(title, globalEntityVolumeConfig.getConfigData().getEntityVolumes().get("pig").getMainVolume(), this::onSave);
    }

    public SliderWidget getSlider() {
        return slider;
    }

    private void onSave(Double value) {
        globalEntityVolumeConfig.getConfigData().getEntityVolumes().get("pig").setMainVolume(value);
    }
}
