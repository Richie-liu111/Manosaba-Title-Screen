package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Replaces any vanilla {@link TitleScreen} the game tries to display with the
 * custom Manosaba title screen. Using {@code @ModifyVariable} on
 * {@link Minecraft#setScreen(Screen)} (rather than injecting into
 * {@code TitleScreen.init}) avoids a re-entrancy loop: the replacement is a
 * subclass of {@code TitleScreen}, so a plain {@code instanceof TitleScreen}
 * guard would fire forever.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Screen manosaba$replaceTitleScreen(Screen screen) {
        if (screen instanceof TitleScreen && !(screen instanceof ManosabaTitleScreen)) {
            return new ManosabaTitleScreen();
        }
        return screen;
    }
}