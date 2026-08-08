package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 替换菜单音乐为 Manosaba 标题曲。
 * 用 @Redirect 拦截 Musics.&lt;clinit&gt; 中的 new Music(...) 构造，
 * 当目标是 MUSIC_MENU 时替换为自定义音乐。
 * <p>
 * 相比 @Shadow+@Final+@Mutable+@Inject 的旧方案，@Redirect
 * 不需要绕过 static final 限制，跨版本/跨加载器兼容性更好。
 * 注意 1.21.10 中字段已改名为 {@code Musics.MENU}（旧 MUSIC_MENU），
 * 但本 mixin 只比对 {@link SoundEvents#MUSIC_MENU} 常量，不受影响。
 */
@Mixin(Musics.class)
public abstract class MusicsMixin {

    @Redirect(method = "<clinit>", at = @At(value = "NEW",
            target = "(Lnet/minecraft/core/Holder;IIZ)Lnet/minecraft/sounds/Music;"))
    private static Music manosaba$redirectMenuMusic(Holder<SoundEvent> eventHolder,
                                                     int minDelay, int maxDelay,
                                                     boolean replaceCurrentMusic) {
        if (eventHolder.value() == SoundEvents.MUSIC_MENU.value()) {
            return new Music(
                    ManosabaSounds.TITLE_MUSIC,
                    50, 50, true);
        }
        return new Music(eventHolder, minDelay, maxDelay, replaceCurrentMusic);
    }
}
