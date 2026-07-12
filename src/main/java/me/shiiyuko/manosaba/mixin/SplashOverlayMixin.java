package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.splash.SplashOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.SplashOverlay;
import net.minecraft.server.packs.resources.ResourceReload;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Shadow
    @Final
    private Minecraft client;

    @Shadow
    @Final
    private ResourceReload reload;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Shadow
    @Final
    private boolean reloading;

    @Shadow
    private long reloadCompleteTime;

    @Shadow
    private long reloadStartTime;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();

        long currentTime = Util.getMeasuringTimeMs();

        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = currentTime;
        }

        float fadeOutProgress = this.reloadCompleteTime > -1L
            ? (float)(currentTime - this.reloadCompleteTime) / 1000.0F
            : -1.0F;
        float fadeInProgress = this.reloadStartTime > -1L
            ? (float)(currentTime - this.reloadStartTime) / 500.0F
            : -1.0F;

        float loadProgress = this.reload.getProgress();

        float logoAlpha = 1.0F;
        if (fadeOutProgress >= 0.0F) {
            logoAlpha = Math.max(0.0F, 1.0F - fadeOutProgress);
        } else if (this.reloading && fadeInProgress < 1.0F) {
            logoAlpha = Math.max(0.15F, fadeInProgress);
        }

        SplashOverlayRenderer.render(loadProgress, logoAlpha);

        if (fadeOutProgress >= 2.0F) {
            this.client.setOverlay((Overlay) null);
            SplashOverlayRenderer.cleanup();
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || fadeInProgress >= 2.0F)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.exceptionHandler.accept(Optional.of(throwable));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.screen != null) {
                this.client.screen.init(this.client, context.guiWidth(), context.guiHeight());
            }
        }
    }
}
