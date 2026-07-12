package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.splash.SplashOverlayRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = LoadingOverlay.class, remap = false)
public abstract class SplashOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ReloadInstance reload;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> onFinish;

    @Shadow
    @Final
    private boolean fadeIn;

    @Shadow
    private long fadeOutStart;

    @Shadow
    private long fadeInStart;

    @Shadow
    private float currentProgress;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();

        long currentTime = Util.getMillis();

        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = currentTime;
        }

        float fadeOutProgress = this.fadeOutStart > -1L
            ? (float)(currentTime - this.fadeOutStart) / 1000.0F
            : -1.0F;
        float fadeInProgress = this.fadeInStart > -1L
            ? (float)(currentTime - this.fadeInStart) / 500.0F
            : -1.0F;

        float loadProgress = this.reload.getActualProgress();

        float logoAlpha = 1.0F;
        if (fadeOutProgress >= 0.0F) {
            logoAlpha = Math.max(0.0F, 1.0F - fadeOutProgress);
        } else if (this.fadeIn && fadeInProgress < 1.0F) {
            logoAlpha = Math.max(0.15F, fadeInProgress);
        }

        SplashOverlayRenderer.render(loadProgress, logoAlpha);

        if (fadeOutProgress >= 2.0F) {
            this.minecraft.setOverlay(null);
            SplashOverlayRenderer.cleanup();
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || fadeInProgress >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, context.guiWidth(), context.guiHeight());
            }
        }
    }
}
