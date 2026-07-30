package com.paulzzh.yuzu.mixins;

import com.paulzzh.yuzu.Manosaba;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 当玩家在菜单界面（!inGame）时，阻止 MusicTicker.update() 运行，
 * 并停掉可能残留的旧音乐。
 * 玩家进入游戏世界（inGame）后放行，让 MusicTicker 正常播游戏 BGM。
 * 参考 YuZuUI-Vintage 的 inGamed 守卫模式。
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique
    private static final Logger LOG = LogManager.getLogger("manosaba:MinecraftMixin");

    @Redirect(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/audio/MusicTicker;update()V"
            )
    )
    private void redirectMusicTicker(MusicTicker instance) {
        if (!Manosaba.inGame) {
            // 菜单界面：停掉 MusicTicker 可能已在播放的音乐（从子界面回来时）
            ISound currentMusic = ((MusicTickerAccessor) instance).getCurrentMusic();
            if (currentMusic != null) {
                LOG.info("stop stray vanilla music & suppress MusicTicker (inGame=false)");
                Minecraft.getMinecraft().getSoundHandler().stopSound(currentMusic);
                ((MusicTickerAccessor) instance).setCurrentMusic(null);
            }
            // 不调用 update() → MusicTicker 不再启动新音乐
        } else {
            // 游戏中：正常更新 MusicTicker
            instance.update();
        }
    }
}
