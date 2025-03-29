package com.github.prests.noisyneighbors.slider;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SoundSliderFactory {
    public static SliderWidget create(Text title, Double initialValue, Consumer<Double> onApply) {
        final Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen == null) {
            throw new IllegalStateException("currentScreen is null in SoundSliderFactory");
        }

        return new SliderWidget(currentScreen.width /2 - 100, currentScreen.height / 2 - 10, 200, 20, Text.translatable("options.percent_value", title, (int) (initialValue * 100.0)), initialValue) {
            @Override
            protected void updateMessage() {
                final Text percentValue = Text.translatable("options.percent_value", title, (int) (this.value * 100.0));
                this.setMessage(percentValue);
            }

            @Override
            protected void applyValue() {
                onApply.accept(this.value);
            }
        };
    }
}
