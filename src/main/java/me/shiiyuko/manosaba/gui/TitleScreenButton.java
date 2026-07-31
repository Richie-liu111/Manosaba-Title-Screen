package me.shiiyuko.manosaba.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.function.AnimationFunction;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Consumer;

/**
 * 标题画面按钮：两张独立 PNG（Normal/Hover）+ hover 检测 + 点击回调。
 * 坐标在虚拟空间内定义，通过 VirtualScreen 转换。支持 alpha 淡入动画。
 * 适配 1.21.1 的 GuiEventListener/NarratableEntry 接口。
 */
public class TitleScreenButton implements Renderable, GuiEventListener, NarratableEntry, Tickable {
    private float x;
    private float y;
    private float width;
    private float height;
    private float alpha;

    /** 碰撞检测尺寸（默认 = 纹理尺寸）。原游戏按钮精灵图比容器大，
     *  碰撞箱应使用容器尺寸，居中于渲染矩形内。 */
    private float collisionW;
    private float collisionH;

    public boolean visible = true;
    private boolean hoverable = true;
    private boolean isHovered = false;
    private boolean isFocused = false;

    private ResourceLocation texture;
    private ResourceLocation textureHover;
    private ResourceLocation labelTexture;
    private float labelWidth;
    private float labelHeight;
    private float labelOffsetX;
    private float labelOffsetY;
    private VirtualScreen virtualScreen;

    private SoundEvent clickSound;
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
        this.collisionW = width;
        this.collisionH = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.isHovered = this.hoverable && this.isMouseOver(mouseX, mouseY);
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
            // 悬停时叠加中文标签（原游戏 Label@ZhHans 精灵，仅简中语言时显示）
            if (this.isHovered && this.labelTexture != null
                    && Minecraft.getInstance().options.languageCode != null
                    && Minecraft.getInstance().options.languageCode.startsWith("zh")) {
                RenderSystem.setShaderTexture(0, labelTexture);
                RenderUtils.blit(
                        virtualScreen.toPracticalX(x + labelOffsetX),
                        virtualScreen.toPracticalY(y + labelOffsetY),
                        virtualScreen.toPracticalWidth(labelWidth),
                        virtualScreen.toPracticalHeight(labelHeight),
                        guiGraphics.pose()
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && this.hoverable && this.isMouseOver(mouseX, mouseY)) {
            if (this.clickSound != null) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(this.clickSound, 1.0f, 1.0f));
            }
            if (this.onClick != null) {
                this.onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        float insetX = (width - collisionW) / 2f;
        float insetY = (height - collisionH) / 2f;
        float virtualX = virtualScreen.toVirtualX((float) mouseX) - x - insetX;
        float virtualY = virtualScreen.toVirtualY((float) mouseY) - y - insetY;
        return virtualX >= 0 && virtualX < collisionW && virtualY >= 0 && virtualY < collisionH;
    }

    @Override
    public void setFocused(boolean b) {
        this.isFocused = b;
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public ScreenRectangle getRectangle() {
        int insetX = (int) ((width - collisionW) / 2f);
        int insetY = (int) ((height - collisionH) / 2f);
        return new ScreenRectangle((int) x + insetX, (int) y + insetY,
                (int) collisionW, (int) collisionH);
    }

    public boolean isHovered() {
        return isHovered;
    }

    public void setOnClick(Consumer<TitleScreenButton> onClick) {
        this.onClick = onClick;
    }

    public void setClickSound(SoundEvent clickSound) {
        this.clickSound = clickSound;
    }

    public void setHoverable(boolean h) {
        this.hoverable = h;
    }

    /** 设置碰撞检测尺寸（居中于渲染矩形）。原游戏容器 < 精灵图，避免透明区域误触。 */
    public void setCollisionSize(float w, float h) {
        this.collisionW = w;
        this.collisionH = h;
    }

    /** 设置悬停时显示的中文标签精灵（原游戏 Label@ZhHans）。
     *  @param offsetX 标签左上角相对于按钮左上角的 X 偏移
     *  @param offsetY 标签左上角相对于按钮左上角的 Y 偏移 */
    public void setLabelTexture(ResourceLocation texture, float w, float h, float offsetX, float offsetY) {
        this.labelTexture = texture;
        this.labelWidth = w;
        this.labelHeight = h;
        this.labelOffsetX = offsetX;
        this.labelOffsetY = offsetY;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

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