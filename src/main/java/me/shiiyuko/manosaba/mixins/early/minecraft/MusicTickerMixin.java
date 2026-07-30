package me.shiiyuko.manosaba.mixins.early.minecraft;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.gui.ManosabaTitleScreen;
import net.minecraft.client.audio.MusicTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 阻止原版 / GTNH 背景音乐在 Manosaba 标题画面体系内播放。
 * 仿 YuZuUI-GTNH：不检查 currentScreen，只用 exit / inGamed 守卫。
 */
@Mixin(value = MusicTicker.class)
public class MusicTickerMixin {
    @Unique
    private static final Logger LOG = LogManager.getLogger("manosaba:MusicTicker");

    @Inject(method = "update", at = @At(value = "HEAD"), cancellable = true)
    public void manosaba$cancelVanillaMusic(CallbackInfo ci) {
        if (!ManosabaConfig.bgm) return;
        if (ManosabaTitleScreen.exit) return;
        if (ManosabaTitleScreen.inGamed) return;
        LOG.info("Cancel MusicTicker.update() — manosaba BGM active");
        ci.cancel();
    }
}
