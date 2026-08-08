package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 抑制 MusicManager：
 * <ol>
 *   <li>{@link Manosaba#titleMusicPlaying} 期间：标题 BGM 由 ManosabaTitleScreen
 *       自行管理（首 tick 播放，黑幕淡出时音乐即响起；子界面期间音乐持续）。
 *       标志位覆盖标题屏及其所有子界面，避免 MusicManager 在子界面
 *       （100 tick 初始延迟/50 tick 间隔后）重启音乐造成叠加。</li>
 *   <li>标题屏已在 LoadingOverlay 淡出前被 setScreen、但其 BGM 尚未启动的窗口
 *       （overlay 非空）：阻止 MusicManager 在此阶段起播原版菜单音乐。</li>
 * </ol>
 */
@Mixin(MusicManager.class)
public class MusicTickerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void manosaba$suppressMusic(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Manosaba.titleMusicPlaying) {
            ci.cancel();
        } else if (mc.screen instanceof ManosabaTitleScreen && mc.getOverlay() != null) {
            ci.cancel();
        }
    }
}
