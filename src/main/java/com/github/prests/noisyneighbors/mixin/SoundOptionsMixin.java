package com.github.prests.noisyneighbors.mixin;

import com.github.prests.noisyneighbors.screen.MainEntitySoundControlScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsMixin extends GameOptionsScreen {
    public SoundOptionsMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Unique
    private void openEntitySoundControlScreen() {
        if (this.body == null || this.client == null) {
            throw new IllegalStateException("client is null in SoundOptionsMixin");
        }

        this.client.setScreen(new MainEntitySoundControlScreen(this, this.gameOptions));
    }

    @Unique
    private ButtonWidget getEntitySoundControlButton() {
        if (this.body == null || this.client == null) {
            throw new IllegalStateException("body is null in SoundOptionsMixin");
        }

        return ButtonWidget.builder(
                Text.of("Entity Sound Control"),
                button -> this.openEntitySoundControlScreen()).size(this.body.getRowWidth(), 20).build();
    }

    @Inject(method = "addOptions", at = @At("HEAD"))
    private void setEntitySoundControlButton(CallbackInfo ci) {
        if (this.body == null || this.client == null) {
            return;
        }

        final ButtonWidget entitySoundControlButton = this.getEntitySoundControlButton();

        this.body.addWidgetEntry(entitySoundControlButton, null);
    }
}
