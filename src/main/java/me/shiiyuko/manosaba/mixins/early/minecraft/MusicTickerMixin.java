package me.shiiyuko.manosaba.mixins.early.minecraft;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.gui.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 阻止原版 {@link MusicTicker} 在 Manosaba 标题画面播放原版菜单音乐。
 * 同时在离开标题画面体系（进世界、退到原版菜单）时停止 manosaba 音乐。
 * 子画面（GuiOptions, GuiSelectWorld 等）切换不会打断音乐。
 */
@Mixin(value = MusicTicker.class)
public class MusicTickerMixin {

    @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
    public void manosaba$replaceMusic(CallbackInfo ci) {
        if (!ManosabaConfig.bgm) return;
        if (ManosabaTitleScreen.exit) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen instanceof ManosabaTitleScreen) {
            // 在 manosaba 标题画面：阻止原版音乐（音乐由 ManosabaTitleScreen.tickMusic() 播放）
            ci.cancel();
            return;
        }

        // 子画面：GuiOptions / GuiSelectWorld / GuiCreateWorld / GuiMultiplayer / GuiLanguage
        // 这些是从标题画面进入的——不阻止原版音乐（它不会被触发），也不停 manosaba 音乐。
        if (mc.currentScreen instanceof GuiOptions
                || mc.currentScreen instanceof GuiSelectWorld
                || mc.currentScreen instanceof GuiCreateWorld
                || mc.currentScreen instanceof GuiMultiplayer
                || mc.currentScreen instanceof GuiLanguage) {
            return;
        }

        // 已脱离标题画面体系（进世界 currentScreen==null，或退到原版 GuiMainMenu）
        // 停止 manosaba 音乐
        if (ManosabaTitleScreen.currentMusic != null
                && mc.getSoundHandler() != null
                && mc.getSoundHandler().isSoundPlaying(ManosabaTitleScreen.currentMusic)) {
            mc.getSoundHandler().stopSound(ManosabaTitleScreen.currentMusic);
            ManosabaTitleScreen.currentMusic = null;
            ManosabaTitleScreen.soundStartTime = 0;
        }
    }
}
