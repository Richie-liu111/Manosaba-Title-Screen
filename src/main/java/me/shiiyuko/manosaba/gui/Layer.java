package me.shiiyuko.manosaba.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.function.AnimationFunction;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

/**
 * 单个图层：一张完整 PNG + 位置 + 缩放 + alpha + 可选的缓动动画。
 * 渲染时调用 RenderUtils.blit 画整张纹理（UV 0~1），坐标通过
 * VirtualScreen 从虚拟空间转换到实际屏幕空间。
 * 来自 YuZuUI 的 Layer，动画系统支持 x/y/alpha/scale 四个维度的缓动。
 */
public class Layer implements Renderable, Tickable {
    private ResourceLocation texture;

    private float x;
    private float y;
    private float width;
    private float height;
    private float scale;

    private float alpha;

    private VirtualScreen virtualScreen;

    public Layer() {
    }

    public Layer(ResourceLocation texture, float x, float y, float width, float height,
                 float scale, float alpha, VirtualScreen virtualScreen) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.alpha = alpha;
        this.virtualScreen = virtualScreen;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, texture);
        RenderUtils.blit(
                virtualScreen.toPracticalX(x),
                virtualScreen.toPracticalY(y),
                virtualScreen.toPracticalWidth(width * scale),
                virtualScreen.toPracticalHeight(height * scale),
                guiGraphics.pose()
        );
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public VirtualScreen getVirtualScreen() {
        return virtualScreen;
    }

    public void setVirtualScreen(VirtualScreen virtualScreen) {
        this.virtualScreen = virtualScreen;
    }

    private Long duration;
    private Long startTime = null;
    private Long delay;
    private AnimationFunction<Float> xFunction;
    private AnimationFunction<Float> yFunction;
    private AnimationFunction<Float> alphaFunction;
    private AnimationFunction<Float> scaleFunction;

    @Override
    public void tick() {
        if (delay == null || duration == 0) {
            return;
        }

        long currentTime = Util.getEpochMillis();
        if (startTime == null) {
            startTime = currentTime;
        } else {
            float t = currentTime - startTime;
            if (t > delay) {
                float time = Math.min((float) (t - delay) / duration, 1);
                if (xFunction != null) {
                    x = xFunction.apply(time, x);
                }
                if (yFunction != null) {
                    y = yFunction.apply(time, y);
                }
                if (alphaFunction != null) {
                    alpha = alphaFunction.apply(time, alpha);
                }
                if (scaleFunction != null) {
                    scale = scaleFunction.apply(time, scale);
                }
            }
        }
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public AnimationFunction<Float> getXFunction() {
        return xFunction;
    }

    public void setXFunction(AnimationFunction<Float> xFunction) {
        this.xFunction = xFunction;
    }

    public AnimationFunction<Float> getYFunction() {
        return yFunction;
    }

    public void setYFunction(AnimationFunction<Float> yFunction) {
        this.yFunction = yFunction;
    }

    public AnimationFunction<Float> getAlphaFunction() {
        return alphaFunction;
    }

    public void setAlphaFunction(AnimationFunction<Float> alphaFunction) {
        this.alphaFunction = alphaFunction;
    }

    public AnimationFunction<Float> getScaleFunction() {
        return scaleFunction;
    }

    public void setScaleFunction(AnimationFunction<Float> scaleFunction) {
        this.scaleFunction = scaleFunction;
    }
}