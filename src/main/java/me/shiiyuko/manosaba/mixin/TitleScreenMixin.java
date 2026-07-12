package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.ui.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, remap = false)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInit(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new ManosabaTitleScreen());
        ci.cancel();
    }
}
