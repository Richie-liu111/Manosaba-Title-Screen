package com.paulzzh.yuzu.mixins;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 访问 MusicTicker 私有字段 currentMusic，配合 MinecraftMixin 停止残留音乐。 */
@Mixin(MusicTicker.class)
public interface MusicTickerAccessor {
    @Accessor("currentMusic")
    ISound getCurrentMusic();

    @Accessor("currentMusic")
    void setCurrentMusic(ISound sound);
}
