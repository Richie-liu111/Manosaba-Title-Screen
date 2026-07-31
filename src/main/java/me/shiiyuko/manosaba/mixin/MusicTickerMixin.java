package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.gui.screen.BootLogoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 抑制 MusicManager（1.21.1 官方映射名）：
 * <ol>
 *   <li>BootLogoScreen 期间：logo 阶段保持安静，并清理加载阶段可能已启动的音乐；</li>
 *   <li>{@link Manosaba#titleMusicPlaying} 期间：标题 BGM 由 ManosabaTitleScreen
 *       自行管理（首 tick 播放，黑幕淡出时音乐即响起；子界面期间音乐持续）。
 *       标志位覆盖标题屏及其所有子界面，避免 MusicManager 在子界面重启音乐造成叠加。</li>
 * </ol>
 */
@Mixin(MusicManager.class)
public class MusicTickerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void manosaba$suppressMusic(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Manosaba.titleMusicPlaying) {
            ci.cancel();
        } else if (mc.screen instanceof BootLogoScreen) {
            // 清理加载阶段可能已播放的音乐（tick 被取消，不会重启）
            mc.getSoundManager().stop();
            ci.cancel();
        }
    }
}
