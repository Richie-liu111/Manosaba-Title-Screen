package me.shiiyuko.manosaba.mixins.early.minecraft;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.gui.BootLogoScreen;
import me.shiiyuko.manosaba.gui.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
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
 * <ol>
 *   <li>BootLogoScreen 期间：logo 阶段保持安静；</li>
 *   <li>ManosabaTitleScreen 体系：标题屏及其子界面期间抑制 MusicTicker，
 *       标题 BGM 由 ManosabaTitleScreen 自行管理。</li>
 * </ol>
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
        // BootLogoScreen 期间保持安静（logo 阶段不播音乐，对齐原游戏 System_Title.nani @bgm 时机）
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof BootLogoScreen) {
            ci.cancel();
            return;
        }
        LOG.info("Cancel MusicTicker.update() — manosaba BGM active");
        ci.cancel();
    }
}
