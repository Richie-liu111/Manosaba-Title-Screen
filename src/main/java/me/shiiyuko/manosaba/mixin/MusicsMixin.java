package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla menu music ({@link Musics#MENU}) with the Manosaba
 * title track. The music manager then handles playback and looping natively,
 * so no manual sound scheduling is required.
 */
@Mixin(Musics.class)
public abstract class MusicsMixin {

    @Mutable
    @Shadow
    @Final
    public static Music MENU;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void manosaba$replaceMenuMusic(CallbackInfo ci) {
        MENU = new Music(
                ManosabaSounds.TITLE_MUSIC.getHolder().orElseThrow(),
                50, 50, true);
    }
}