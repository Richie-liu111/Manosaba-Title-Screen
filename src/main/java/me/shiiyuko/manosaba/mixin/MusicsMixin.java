package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 替换菜单音乐为 Manosaba 标题曲。
 * 用 @Redirect 拦截 Musics.<clinit> 中的 new Music(...)，
 * 当目标是 MUSIC_MENU 时替换为 manosaba:music。
 * 这是 YuZuUI 在 NeoForge 1.21.1 的写法。
 */
@Mixin(Musics.class)
public abstract class MusicsMixin {

    @Redirect(method = "<clinit>", at = @At(value = "NEW",
            target = "(Lnet/minecraft/core/Holder;IIZ)Lnet/minecraft/sounds/Music;"))
    private static Music manosaba$redirectMenuMusic(Holder<SoundEvent> eventHolder,
                                                     int minDelay, int maxDelay,
                                                     boolean replaceCurrentMusic) {
        if (eventHolder.value() == SoundEvents.MUSIC_MENU.value()) {
            return new Music(ManosabaSounds.TITLE_MUSIC, 50, 50, true);
        }
        return new Music(eventHolder, minDelay, maxDelay, replaceCurrentMusic);
    }
}