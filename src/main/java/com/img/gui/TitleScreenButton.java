package com.img.gui;

import com.img.function.AnimationFunction;
import com.paulzzh.yuzu.gui.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.function.Consumer;

/**
 * 标题画面按钮：两张独立 PNG（Normal/Hover）+ hover 检测 + 点击回调。
 * 坐标在虚拟空间内定义，通过 VirtualScreen 转换。支持 alpha 淡入动画。
 * 来自 YuZuUI 的 TitleScreenButton，1.12.2 版本。
 */
public class TitleScreenButton {
    private float x;
    private float y;
    private float width;
    private float height;
    private float alpha;

    /** 碰撞检测尺寸（默认 = 纹理尺寸）。 */
    private float collisionW;
    private float collisionH;

    public boolean visible = true;
    private boolean isHovered = false;

    private ResourceLocation texture;
    private ResourceLocation textureHover;
    private ResourceLocation labelTexture;
    private float labelWidth;
    private float labelHeight;
    private float labelOffsetX;
    private float labelOffsetY;
    private VirtualScreen virtualScreen;

    private Consumer<TitleScreenButton> onClick;
    private SoundEvent clickSound;

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

    public void render(int mouseX, int mouseY) {
        if (this.visible) {
            this.isHovered = this.isMouseOver(mouseX, mouseY);
            ResourceLocation tex = this.isHovered ? textureHover : texture;
            Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            RenderUtils.blit(virtualScreen, x, y, width, height);

            // 悬停时叠加中文标签
            if (this.isHovered && this.labelTexture != null
                    && Minecraft.getMinecraft().gameSettings.language != null
                    && Minecraft.getMinecraft().gameSettings.language.startsWith("zh")) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(labelTexture);
                RenderUtils.blit(
                        virtualScreen,
                        x + labelOffsetX, y + labelOffsetY,
                        labelWidth, labelHeight
                );
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.visible && mouseButton == 0 && this.isMouseOver(mouseX, mouseY)) {
            if (this.clickSound != null) {
                Minecraft.getMinecraft().getSoundHandler().playSound(
                        PositionedSoundRecord.getMasterRecord(this.clickSound, 1.0f));
            }
            if (this.onClick != null) {
                this.onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        float insetX = (width - collisionW) / 2f;
        float insetY = (height - collisionH) / 2f;
        float virtualX = virtualScreen.toVirtualX(mouseX) - x - insetX;
        float virtualY = virtualScreen.toVirtualY(mouseY) - y - insetY;
        return virtualX >= 0 && virtualX < collisionW && virtualY >= 0 && virtualY < collisionH;
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

    public void setCollisionSize(float w, float h) {
        this.collisionW = w;
        this.collisionH = h;
    }

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

    public void tick() {
        if (delay == null || duration == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();
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
