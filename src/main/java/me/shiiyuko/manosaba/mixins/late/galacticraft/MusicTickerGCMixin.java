package me.shiiyuko.manosaba.mixins.late.galacticraft;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.gui.ManosabaTitleScreen;
import micdoodle8.mods.galacticraft.core.client.sounds.MusicTickerGC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截 Galacticraft 替换后的 MusicTickerGC，阻止 GTNH 背景音乐。
 * 仅当 Galacticraft 存在时由 LATE phase 加载。
 * 仿 YuZuUI-GTNH 的 MusicTickerGCMixin。
 */
@Mixin(value = MusicTickerGC.class)
public class MusicTickerGCMixin {

    @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
    public void manosaba$cancelGCMusic(CallbackInfo ci) {
        if (!ManosabaConfig.bgm) return;
        if (ManosabaTitleScreen.exit) return;
        if (ManosabaTitleScreen.inGamed) return;
        ci.cancel();
    }
}
