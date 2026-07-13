package me.shiiyuko.manosaba.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.function.AnimationFunction;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * 标题画面按钮：两张独立 PNG（Normal/Hover）+ hover 检测 + 点击回调。
 * 坐标在虚拟空间内定义，通过 {@link VirtualScreen} 转换。支持 alpha 淡入动画。
 * 来自 YuZuUI 的 TitleScreenButton，简化了不必要的接口实现。
 */
public class TitleScreenButton implements Renderable, GuiEventListener, NarratableEntry, Tickable {
    private float x;
    private float y;
    private float width;
    private float height;
    private float alpha;

    public boolean visible = true;
    private boolean isHovered = false;

    private ResourceLocation texture;
    private ResourceLocation textureHover;
    private VirtualScreen virtualScreen;

    private Consumer<TitleScreenButton> onClick;

    public TitleScreenButton(float x, float y, float width, float height,
                             ResourceLocation texture, ResourceLocation textureHover,
                             VirtualScreen virtualScreen, float alpha) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.textureHover = textureHover;
        this.virtualScreen = virtualScreen;
        this.alpha = alpha;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.isHovered = this.isMouseOver(mouseX, mouseY);
            if (this.isHovered) {
                RenderSystem.setShaderTexture(0, textureHover);
            } else {
                RenderSystem.setShaderTexture(0, texture);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            RenderUtils.blit(
                    virtualScreen.toPracticalX(x),
                    virtualScreen.toPracticalY(y),
                    virtualScreen.toPracticalWidth(width),
                    virtualScreen.toPracticalHeight(height),
                    guiGraphics.pose()
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && this.isMouseOver(mouseX, mouseY)) {
            if (this.onClick != null) {
                this.onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        float virtualX = virtualScreen.toVirtualX((float) mouseX) - x;
        float virtualY = virtualScreen.toVirtualY((float) mouseY) - y;
        return virtualX >= 0 && virtualX < width && virtualY >= 0 && virtualY < height;
    }

    @Override
    public void setFocused(boolean b) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    public boolean isHovered() {
        return isHovered;
    }

    public void setOnClick(Consumer<TitleScreenButton> onClick) {
        this.onClick = onClick;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    /**
     * 动画相关
     */
    private Long duration;
    private Long startTime = null;
    private Long delay;
    private AnimationFunction<Float> alphaFunction;

    public void setAlphaFunction(AnimationFunction<Float> alphaFunction) {
        this.alphaFunction = alphaFunction;
    }

    @Override
    public void tick() {
        if (delay == null || duration == 0) {
            return;
        }

        long currentTime = Util.getEpochMillis();
        if (startTime == null) {
            startTime = currentTime;
        } else {
            long t = currentTime - startTime;
            if (t > delay) {
                float time = Math.min((float) (t - delay) / duration, 1);
                if (alphaFunction != null) {
                    alpha = alphaFunction.apply(time, alpha);
                }
            }
        }
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}