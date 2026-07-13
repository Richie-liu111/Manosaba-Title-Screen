package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.gui.splash.SplashOverlayRenderer;
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

/**
 * 替换 LoadingOverlay.render 为 Manosaba splash art，保留原版的
 * reload 完成检测和淡入淡出时序。字段名来自 1.21.1 official mappings。
 */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void manosaba$renderSplash(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                       float partialTick, CallbackInfo ci) {
        ci.cancel();

        long now = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = now;
        }

        float fadeOutProgress = this.fadeOutStart > -1L
                ? (float) (now - this.fadeOutStart) / 1000.0F
                : -1.0F;
        float fadeInProgress = this.fadeInStart > -1L
                ? (float) (now - this.fadeInStart) / 500.0F
                : -1.0F;

        float logoAlpha = 1.0F;
        if (fadeOutProgress >= 0.0F) {
            logoAlpha = Math.max(0.0F, 1.0F - fadeOutProgress);
        } else if (this.fadeIn && fadeInProgress < 1.0F) {
            logoAlpha = Math.max(0.15F, fadeInProgress);
        }

        SplashOverlayRenderer.get().render(guiGraphics, logoAlpha);

        if (fadeOutProgress >= 2.0F) {
            this.minecraft.setOverlay(null);
            SplashOverlayRenderer.get().cleanup();
        }

        if (this.fadeOutStart == -1L && this.reload.isDone()
                && (!this.fadeIn || fadeInProgress >= 2.0F)) {
            this.fadeOutStart = Util.getMillis();
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            if (this.minecraft.screen != null) {
                this.minecraft.screen.resize(this.minecraft,
                        Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }
        }
    }
}