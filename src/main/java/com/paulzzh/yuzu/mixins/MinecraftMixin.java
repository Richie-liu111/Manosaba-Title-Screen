package com.paulzzh.yuzu.mixins;

import com.paulzzh.yuzu.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 当 Manosaba 标题界面打开时，阻止原版 MusicTicker 播放背景音乐
 * （BGM 由 ManosabaTitleScreen 自行管理）。
 * 参考 YuZuUI-Vintage 的 MinecraftMixin 实现。
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Redirect(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/audio/MusicTicker;update()V"
            )
    )
    private void redirectMusicTicker(MusicTicker instance) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof ManosabaTitleScreen)) {
            instance.update();
        }
        // 在 ManosabaTitleScreen 上时，不调用 MusicTicker.update() → 原版音乐永不播放
    }
}
