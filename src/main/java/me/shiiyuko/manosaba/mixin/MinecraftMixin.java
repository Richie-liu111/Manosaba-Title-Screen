package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 把任何 vanilla TitleScreen 替换为 ManosabaTitleScreen。
 * 用 @ModifyVariable 而非注入 TitleScreen.init，避免重入循环
 * （ManosabaTitleScreen 是 TitleScreen 的子类）。
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